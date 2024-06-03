package com.code.salesappbackend.mappers;

import com.code.salesappbackend.dtos.requests.ProviderDto;
import com.code.salesappbackend.exceptions.DataExistsException;
import com.code.salesappbackend.models.Provider;
import com.code.salesappbackend.models.enums.Status;
import com.code.salesappbackend.services.interfaces.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProviderMapper {
    private final ProviderService providerService;
    private final AddressMapper addressMapper;

    public Provider providerDto2Provider(ProviderDto providerDto) throws DataExistsException {
        providerService.checkExistsProviderName(providerDto.getProviderName());
        Provider provider = new Provider();
        provider.setProviderName(providerDto.getProviderName());
        provider.setAddress(addressMapper.addressDto2Address(providerDto.getAddress()));
        provider.setStatus(Status.ACTIVE);
        provider.setEmail(providerDto.getEmail());
        provider.setPhoneNumber(providerDto.getPhoneNumber());
        return provider;
    }
}
