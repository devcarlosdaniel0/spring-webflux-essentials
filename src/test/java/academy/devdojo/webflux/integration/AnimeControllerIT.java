package academy.devdojo.webflux.integration;

import academy.devdojo.webflux.domain.Anime;
import academy.devdojo.webflux.exception.CustomAttributes;
import academy.devdojo.webflux.repository.AnimeRepository;
import academy.devdojo.webflux.service.AnimeService;
import academy.devdojo.webflux.util.AnimeCreator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class AnimeControllerIT {
    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient webTestClient;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @Test
    void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @BeforeEach
    void setUp() {
        BDDMockito.when(animeRepository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.save(AnimeCreator.animeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository
                        .saveAll(List.of(AnimeCreator.animeToBeSaved(), AnimeCreator.animeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(animeRepository.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.save(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("findAll returns a Flux of Anime when successful")
    void findAll_Flavor1_ReturnsFluxOfAnime_WhenSuccessful() {
        webTestClient.get()
                .uri("/animes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("findAll returns a Flux of Anime when successful")
    void findAll_Flavor2_ReturnsFluxOfAnime_WhenSuccessful() {
        webTestClient.get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns a Mono with Anime when exists")
    void findById_ReturnsMonoWithAnime_WhenExists() {
        webTestClient.get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns a Mono error when anime does not exists")
    void findById_ReturnsMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");

    }

    @Test
    @DisplayName("save creates an anime when successful")
    void save_CreatesAnAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.animeToBeSaved();

        webTestClient.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("saveBatch creates a list of anime when successful")
    void saveBatch_CreatesListOfAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.animeToBeSaved();

        webTestClient.post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime);
    }

    @Test
    @DisplayName("save returns Mono error when name is empty")
    void save_ReturnsError_WhenNameIsEmpty() {
        Anime animeToBeSaved = AnimeCreator.animeToBeSaved().withName(" ");

        webTestClient.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("saveBatch returns Mono error when one of the objects in the list contains null or empty name")
    void saveBatch_ReturnsMonoError_WhenContainsNullOrEmptyName() {
        Anime animeToBeSaved = AnimeCreator.animeToBeSaved();

        BDDMockito.when(animeRepository
                        .saveAll(ArgumentMatchers.anyIterable()))
                        .thenReturn(Flux.just(anime, anime.withName(" ")));

        webTestClient.post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved.withName(" "))))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    void delete_RemovesTheAnime_WhenSuccessful() {
        webTestClient.delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete returns mono error when anime does not exists")
    void delete_ReturnsMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }

    @Test
    @DisplayName("update save updated anime and returns an empty mono when successful")
    void update_SaveUpdatedAnime_WhenSuccessful() {
        webTestClient.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("update returns Mono error when anime does not exists")
    void update_ReturnsMonoError_WhenAnimeDoesNotExits() {
        BDDMockito.when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient.put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.developerMessage").isEqualTo("A ResponseStatusException Happened");
    }
}
