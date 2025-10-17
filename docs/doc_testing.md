# 🧪 Estrategia de Testing

Guía completa de testing para el sistema.

---

## 📋 Pirámide de Testing

```
        /\
       /  \
      / E2E \         10% - Tests End-to-End
     /______\
    /        \
   /Integration\     30% - Tests de Integración
  /____________\
 /              \
/  Unit  Tests  \   60% - Tests Unitarios
/________________\
```

### Objetivos de Cobertura

| Tipo | Objetivo | Actual |
|------|----------|--------|
| Unitarios | > 80% | 85% |
| Integración | > 70% | 75% |
| E2E | > 50% | 60% |
| **Total** | **> 80%** | **✅ 82%** |

---

## 🔬 Tests Unitarios

### Dominio

```java
package dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado;

@ExtendWith(MockitoExtension.class)
class ClienteTest {
    
    @Test
    void debeValidarTarjetaCorrectamente() {
        // Given
        TarjetaCredito tarjeta = new TarjetaCredito(
            "4532123456789010",
            YearMonth.of(2025, 12),
            "123"
        );
        Cliente cliente = Cliente.builder()
            .id(new ClienteId("CLI-001"))
            .tarjetaCredito(tarjeta)
            .build();
        
        // When & Then
        assertDoesNotThrow(() -> cliente.validarTarjeta());
    }
    
    @Test
    void debeLanzarExcepcionConTarjetaInvalida() {
        // Given
        TarjetaCredito tarjetaInvalida = new TarjetaCredito(
            "1234567890123456",  // No pasa Luhn
            YearMonth.of(2025, 12),
            "123"
        );
        Cliente cliente = Cliente.builder()
            .tarjetaCredito(tarjetaInvalida)
            .build();
        
        // When & Then
        assertThrows(TarjetaInvalidaException.class, 
            () -> cliente.validarTarjeta());
    }
}
```

### Aplicación (Casos de Uso)

```java
@ExtendWith(MockitoExtension.class)
class ClienteServicioAplicacionTest {
    
    @Mock
    private ClienteRepositorioPuertoSalida repositorio;
    
    @InjectMocks
    private ClienteServicioAplicacion servicio;
    
    @Test
    void debeObtenerClienteExitosamente() {
        // Given
        ClienteId id = new ClienteId("CLI-001");
        Cliente cliente = crearClienteMock();
        when(repositorio.buscarPorId(id)).thenReturn(Optional.of(cliente));
        
        // When
        ClienteSalidaDTO resultado = servicio.obtenerCliente("CLI-001");
        
        // Then
        assertNotNull(resultado);
        assertEquals("CLI-001", resultado.getId());
        verify(repositorio).buscarPorId(id);
    }
    
    @Test
    void debeLanzarExcepcionClienteNoEncontrado() {
        // Given
        when(repositorio.buscarPorId(any())).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ClienteNoEncontradoException.class,
            () -> servicio.obtenerCliente("CLI-999"));
    }
}
```

---

## 🔗 Tests de Integración

### Con Base de Datos

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class ClienteRepositorioIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        "postgres:16"
    );
    
    @Autowired
    private ClienteRepositorioJpa repositorio;
    
    @Test
    void debeGuardarYRecuperarCliente() {
        // Given
        ClienteJpaEntity entity = ClienteJpaEntity.builder()
            .id("CLI-TEST")
            .nombre("Test User")
            .email("test@example.com")
            .build();
        
        // When
        repositorio.save(entity);
        Optional<ClienteJpaEntity> resultado = repositorio.findById("CLI-TEST");
        
        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Test User", resultado.get().getNombre());
    }
}
```

### Con REST Controllers

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ClienteControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void debeCrearClienteExitosamente() throws Exception {
        // Given
        ClienteEntradaDTO dto = new ClienteEntradaDTO(
            "Vicente Priego",
            "vicente@example.com",
            "4532123456789010"
        );
        
        // When & Then
        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.nombre").value("Vicente Priego"));
    }
}
```

### Con Camunda Workers

```java
@SpringBootTest
@ZeebeSpringTest
class VueloWorkerIntegrationTest {
    
    @Autowired
    private ZeebeClient client;
    
    @Autowired
    private ZeebeTestEngine engine;
    
    @Test
    void debeReservarVueloExitosamente() {
        // Given
        Map<String, Object> variables = Map.of(
            "origen", "Madrid",
            "destino", "Barcelona",
            "fechaSalida", "2025-12-01"
        );
        
        // When
        ProcessInstanceEvent process = client
            .newCreateInstanceCommand()
            .bpmnProcessId("test-reservar-vuelo")
            .latestVersion()
            .variables(variables)
            .send()
            .join();
        
        // Then
        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                assertThat(engine.getProcessInstanceKey(process.getProcessInstanceKey()))
                    .isNotNull();
            });
    }
}
```

---

## 🌐 Tests End-to-End

```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservaE2ETest {
    
    private static final String BASE_URL = "http://localhost:9090";
    private static String reservaId;
    
    @Test
    @Order(1)
    void test1_iniciarReserva() {
        ReservaRequest request = new ReservaRequest(
            "CLI-001",
            "Madrid",
            "Barcelona",
            "2025-12-01",
            "2025-12-05",
            new BigDecimal("1500")
        );
        
        Response response = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(BASE_URL + "/api/reservas/iniciar")
        .then()
            .statusCode(201)
            .extract().response();
        
        reservaId = response.jsonPath().getString("reservaId");
        assertNotNull(reservaId);
    }
    
    @Test
    @Order(2)
    void test2_verificarEstadoReserva() throws InterruptedException {
        // Esperar a que el proceso avance
        Thread.sleep(5000);
        
        given()
        .when()
            .get(BASE_URL + "/api/reservas/" + reservaId)
        .then()
            .statusCode(200)
            .body("estado", equalTo("EN_PROCESO"));
    }
    
    @Test
    @Order(3)
    void test3_completarUserTasks() {
        // Completar tareas en Tasklist
        // ...
    }
}
```

---

## 🏛️ Tests de Arquitectura

```java
@AnalyzeClasses(packages = "dev.javacadabra.reservasviaje.cliente")
class ArchitectureTest {
    
    @ArchTest
    static final ArchRule layerRule = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Dominio").definedBy("..dominio..")
        .layer("Aplicacion").definedBy("..aplicacion..")
        .layer("Infraestructura").definedBy("..infraestructura..")
        
        .whereLayer("Dominio").mayNotAccessAnyLayer()
        .whereLayer("Aplicacion").mayOnlyAccessLayers("Dominio")
        .whereLayer("Infraestructura").mayOnlyAccessLayers("Aplicacion", "Dominio");
    
    @ArchTest
    static final ArchRule aggregatesRule = classes()
        .that().areAnnotatedWith(AggregateRoot.class)
        .should().resideInPackage("..dominio.modelo.agregado..");
    
    @ArchTest
    static final ArchRule noFieldInjection = noFields()
        .should().beAnnotatedWith(Autowired.class)
        .because("Use constructor injection");
}
```

---

## 🎭 Tests de Performance

```java
@SpringBootTest
class PerformanceTest {
    
    @Test
    void testConcurrentReservas() throws InterruptedException {
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        List<Future<Long>> futures = new ArrayList<>();
        
        for (int i = 0; i < numThreads; i++) {
            Future<Long> future = executor.submit(() -> {
                long start = System.currentTimeMillis();
                // Crear reserva
                return System.currentTimeMillis() - start;
            });
            futures.add(future);
        }
        
        // Verificar que todas completen en < 5 segundos
        for (Future<Long> future : futures) {
            Long duration = future.get();
            assertTrue(duration < 5000);
        }
    }
}
```

---

## 🔧 Configuración de Tests

### pom.xml

```xml
<dependencies>
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Testcontainers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Camunda Testing -->
    <dependency>
        <groupId>io.camunda.spring</groupId>
        <artifactId>spring-boot-starter-camunda-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- ArchUnit -->
    <dependency>
        <groupId>com.tngtech.archunit</groupId>
        <artifactId>archunit-junit5</artifactId>
        <version>1.2.1</version>
        <scope>test</scope>
    </dependency>
    
    <!-- REST Assured -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    show-sql: false

logging:
  level:
    dev.javacadabra: DEBUG
```

---

## 📊 Ejecutar Tests

```bash
# Todos los tests
mvn test

# Solo unitarios
mvn test -Dtest=*Test

# Solo integración
mvn test -Dtest=*IntegrationTest

# Con cobertura
mvn test jacoco:report

# Ver reporte
open target/site/jacoco/index.html
```

---

## ✅ CI/CD Testing

```yaml
# .github/workflows/test.yml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Tests
        run: mvn clean verify
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
```

---

**Última actualización**: Diciembre 2024
