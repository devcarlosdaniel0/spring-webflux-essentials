package academy.devdojo.webflux.util;

import academy.devdojo.webflux.domain.Anime;

public class AnimeCreator {

    public static Anime animeToBeSaved() {
        return Anime.builder()
                .name("Dragon Ball Z")
                .build();
    }

    public static Anime createValidAnime() {
        return Anime.builder()
                .id(1)
                .name("Dragon Ball Z")
                .build();
    }

    public static Anime createValidUpdatedAnime() {
        return Anime.builder()
                .id(1)
                .name("Dragon Ball Super")
                .build();
    }
}
