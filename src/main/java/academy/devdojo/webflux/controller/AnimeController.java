package academy.devdojo.webflux.controller;

import academy.devdojo.webflux.domain.Anime;
import academy.devdojo.webflux.service.AnimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("animes")
@Slf4j
public class AnimeController {
    private final AnimeService animeService;

    @GetMapping
    public Flux<Anime> findAll() {
        return animeService.findAll();
    }

    @GetMapping(path = "{id}")
    public Mono<Anime> findById(@PathVariable int id) {
        return animeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Anime> create(@Valid @RequestBody Anime anime) {
        return animeService.save(anime);
    }

    @PutMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> update(@PathVariable int id, @Valid @RequestBody Anime anime) {
        return animeService.update(anime.withId(id));
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable int id) {
        return animeService.delete(id);
    }
}
