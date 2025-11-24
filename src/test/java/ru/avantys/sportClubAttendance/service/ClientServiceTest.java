package ru.avantys.sportClubAttendance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.avantys.sportClubAttendance.dto.ClientDto;
import ru.avantys.sportClubAttendance.model.Client;
import ru.avantys.sportClubAttendance.repository.ClientRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    ClientRepository clientRepository;

    ClientService clientService;

    @BeforeEach
    public void setUp(){
        clientService = new ClientService(clientRepository);
    }

    @Test
    public void testCreateClientSuccess(){
        when(clientRepository.existsByEmail(eq("test@test.ru"))).thenReturn(false);
        when(clientRepository.save(any())).thenReturn(builtDefailtClient());

        Client client = clientService.createClient(builtDefaultClientDto());

        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), client.getId());
        assertEquals("test@test.com", client.getEmail());
        assertEquals("Иванов Иван Иванович", client.getFullName());
        assertEquals(false, client.getIsBlocked());

        verify(clientRepository, times(1)).save(any());
    }

    @Test
    public void testCreateClientFailWhereEmailRepeated(){
        when(clientRepository.existsByEmail(eq("test@test.com"))).thenReturn(true);

        var clientDto = builtDefaultClientDto();
        assertThrows(IllegalArgumentException.class, () ->
                clientService.createClient(clientDto));

        verify(clientRepository, never()).save(any());
    }

    @Test
    public void testUpdateClientSuccess(){
        when(clientRepository.findById(eq(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .thenReturn(Optional.of(builtDefailtClient()));
        when(clientRepository.existsByEmail(eq("alexAvantys@avantys.corp"))).thenReturn(false);
        when(clientRepository.save(any())).thenReturn(builtDefailtClient());

        var clientDto = new ClientDto(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Иванов Алексей Иванович",
                "alexAvantys@avantys.corp",
                false
        );
        clientService.updateClient(UUID.fromString("00000000-0000-0000-0000-000000000001"), clientDto);

        verify(clientRepository, times(1)).save(any());
    }

    // TODO(Доработать тест)
    @Test
    public void testUpdateClientFailWhereClientNotExist(){
        when(clientRepository.findById(eq(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .thenReturn(Optional.of(builtDefailtClient()));
        when(clientRepository.existsByEmail(eq("test@test.com"))).thenReturn(false);
        when(clientRepository.save(any())).thenReturn(builtDefailtClient());

        var clientDto = builtDefaultClientDto();
        Client client = clientService.updateClient(UUID.fromString("00000000-0000-0000-0000-000000000001"), clientDto);

        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), client.getId());
        assertEquals("test@test.com", client.getEmail());
        assertEquals("Иванов Иван Иванович", client.getFullName());
        assertEquals(false, client.getIsBlocked());

        verify(clientRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateClientSuccessWithNullParameters(){
        // TODO("Написать тест как отработает с нулевыми значениями)
    }

    @Test
    public void testUpdateClientFailWhereEmailRepeated(){
        when(clientRepository.findById(eq(UUID.fromString("00000000-0000-0000-0000-000000000001"))))
                .thenReturn(Optional.of(builtDefailtClient()));
        when(clientRepository.existsByEmail(eq("alexAvantys@avantys.corp"))).thenReturn(true);

        var clientDto = new ClientDto(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Иванов Алексей Иванович",
                "alexAvantys@avantys.corp",
                false
        );
        assertThrows(IllegalArgumentException.class, () ->
                clientService.updateClient(UUID.fromString("00000000-0000-0000-0000-000000000001"), clientDto));

        verify(clientRepository, never()).save(any());
    }

    private Client builtDefailtClient(){
        var client = new Client();
        client.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        client.setEmail("test@test.com");
        client.setFullName("Иванов Иван Иванович");
        client.setIsBlocked(false);
        return client;
    }

    private ClientDto builtDefaultClientDto() {
        return new ClientDto(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "Иванов Иван Иванович",
                "test@test.com",
                false
        );
    }
}