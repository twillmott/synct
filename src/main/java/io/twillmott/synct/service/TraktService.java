package io.twillmott.synct.service;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.enums.Extended;
import io.twillmott.synct.domain.ShowEntity;
import io.twillmott.synct.events.event.TraktAuthorizedEvent;
import io.twillmott.synct.repository.ShowRepository;
import io.twillmott.synct.service.mapper.traktjava.ShowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@Slf4j
public class TraktService {

    private final TraktV2 trakt;
    private final ShowRepository showRepository;

    @Autowired
    public TraktService(TraktV2 trakt, ShowRepository showRepository) {
        this.trakt = trakt;
        this.showRepository = showRepository;
    }

    @EventListener
    void traktAuthorizedEventListener(TraktAuthorizedEvent event) {
        CompletableFuture.runAsync(() -> syncTraktLibrary(true));
    }

    @Async
    @Scheduled(fixedRate = 30 * 60 * 1000)
    void syncTraktLibrary() {
        syncTraktLibrary(true);
    }

    public void syncTraktLibrary(boolean fullResync) {
        log.info("Starting synchronisation of Trakt Account");

        // Skip if not authorised
        if (isNull(trakt.accessToken())) {
            return;
        }

        trakt.sync().collectionShows(Extended.FULL).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<BaseShow>> call, Response<List<BaseShow>> response) {
                if (response.isSuccessful()) {
                    List<ShowEntity> shows = response.body().stream()
                            .map(ShowMapper::toEntity)
                            .collect(Collectors.toList());
                    shows.stream().forEach(s -> log.info(s.toString()));
                    showRepository.saveAll(shows);
                }
            }

            @Override
            public void onFailure(Call<List<BaseShow>> call, Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

}
