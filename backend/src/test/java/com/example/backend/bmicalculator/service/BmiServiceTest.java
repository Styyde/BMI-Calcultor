package com.example.backend.bmicalculator.service;

import com.example.backend.bmicalculator.dto.BmiRequest;
import com.example.backend.bmicalculator.dto.BmiResponse;
import com.example.backend.bmicalculator.entity.BmiRecord;
import com.example.backend.bmicalculator.entity.User;
import com.example.backend.bmicalculator.repository.BmiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BmiServiceTest {

    @Mock
    private BmiRepository bmiRepository;

    private BmiService bmiService;

    @BeforeEach
    void setUp() {
        bmiService = new BmiService(bmiRepository);
    }

    @Test
    void calculateAndSave_computesBmiAndCategory_forAnonymousUser() {
        BmiRequest request = new BmiRequest(70.0, 175.0);

        BmiResponse response = bmiService.calculateAndSave(request, "127.0.0.1", null);

        assertThat(response.getBmi()).isEqualTo(22.9);
        assertThat(response.getCategory()).isEqualTo("NORMAL");
        assertThat(response.getWeight()).isEqualTo(70.0);
        assertThat(response.getHeight()).isEqualTo(175.0);

        ArgumentCaptor<BmiRecord> captor = ArgumentCaptor.forClass(BmiRecord.class);
        verify(bmiRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isNull();
        assertThat(captor.getValue().getIpAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    void calculateAndSave_associatesRecordWithLoggedInUser() {
        BmiRequest request = new BmiRequest(90.0, 180.0);
        User user = new User("john@doe.com", "hash", "John", "Doe");

        bmiService.calculateAndSave(request, "10.0.0.5", user);

        ArgumentCaptor<BmiRecord> captor = ArgumentCaptor.forClass(BmiRecord.class);
        verify(bmiRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    void calculateAndSaveImperial_convertsUnitsBeforeCalculating() {
        // 150 lbs, 5'10" -> 68.0 kg, 177.8 cm -> BMI ~21.5
        BmiResponse response = bmiService.calculateAndSaveImperial(150, 5, 10, "127.0.0.1", null);

        assertThat(response.getWeight()).isEqualTo(68.0);
        assertThat(response.getHeight()).isEqualTo(177.8);
        assertThat(response.getCategory()).isEqualTo("NORMAL");
    }

    @Test
    void getHistory_withLoggedInUser_queriesByUserId() {
        User user = new User("john@doe.com", "hash", "John", "Doe");
        user.setId(42L);
        BmiRecord record = new BmiRecord(user, "10.0.0.5", 70.0, 175.0, 22.9, "NORMAL");

        when(bmiRepository.findTop10ByUserIdOrderByCalculatedAtDesc(42L)).thenReturn(List.of(record));

        List<BmiResponse> history = bmiService.getHistory("10.0.0.5", user, 10);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getBmi()).isEqualTo(22.9);
        verify(bmiRepository).findTop10ByUserIdOrderByCalculatedAtDesc(42L);
        verify(bmiRepository, never()).findTop10ByIpAddressOrderByCalculatedAtDesc(anyString());
    }

    @Test
    void getHistory_withoutUser_fallsBackToIpAddress() {
        BmiRecord record = new BmiRecord("10.0.0.5", 70.0, 175.0, 22.9, "NORMAL");
        when(bmiRepository.findTop10ByIpAddressOrderByCalculatedAtDesc("10.0.0.5"))
                .thenReturn(List.of(record));

        List<BmiResponse> history = bmiService.getHistory("10.0.0.5", null, 10);

        assertThat(history).hasSize(1);
        verify(bmiRepository).findTop10ByIpAddressOrderByCalculatedAtDesc("10.0.0.5");
        verify(bmiRepository, never()).findTop10ByUserIdOrderByCalculatedAtDesc(anyLong());
    }

    @Test
    void getHistory_respectsLimitEvenWhenRepositoryReturnsMore() {
        BmiRecord r1 = new BmiRecord("10.0.0.5", 70.0, 175.0, 22.9, "NORMAL");
        BmiRecord r2 = new BmiRecord("10.0.0.5", 71.0, 175.0, 23.2, "NORMAL");
        when(bmiRepository.findTop10ByIpAddressOrderByCalculatedAtDesc("10.0.0.5"))
                .thenReturn(List.of(r1, r2));

        List<BmiResponse> history = bmiService.getHistory("10.0.0.5", null, 1);

        assertThat(history).hasSize(1);
    }
}
