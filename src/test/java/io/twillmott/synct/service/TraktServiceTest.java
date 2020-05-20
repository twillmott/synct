package io.twillmott.synct.service;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.entities.Show;
import com.uwetrottmann.trakt5.entities.ShowIds;
import com.uwetrottmann.trakt5.services.Sync;
import io.twillmott.synct.domain.ShowEntity;
import io.twillmott.synct.repository.ShowRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraktServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    TraktV2 traktV2;

    @Mock
    ShowRepository showRepository;

    @InjectMocks
    TraktService subject;

    @Test
    public void syncLibrary_whenReturnsSuccess() {

        // Given
        Sync sync = mock(Sync.class);
        Call call = mock(Call.class);
        when(traktV2.accessToken()).thenReturn("access");
        when(traktV2.sync()).thenReturn(sync);
        when(sync.collectionShows(any())).thenReturn(call);

        Show show = new Show();
        show.ids = new ShowIds();
        BaseShow baseShow = new BaseShow();
        baseShow.show = show;
        baseShow.seasons = Lists.newArrayList();

        doAnswer(invocationOnMock -> {
            Callback callback = invocationOnMock.getArgument(0, Callback.class);
            callback.onResponse(call, Response.success(Lists.newArrayList(baseShow)));
            return null;
        }).when(call).enqueue(any());

        // When
        subject.syncTraktLibrary(true);

        // Then
        ArgumentCaptor<List<ShowEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(showRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }
}
