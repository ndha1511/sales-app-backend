package com.code.salesappbackend.services.impls;

import com.code.salesappbackend.models.UserVoucher;
import com.code.salesappbackend.models.id_classes.UserVoucherId;
import com.code.salesappbackend.services.interfaces.UserVoucherService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class UserVoucherImpl extends BaseServiceImpl<UserVoucher, UserVoucherId> implements UserVoucherService {
    public UserVoucherImpl(JpaRepository<UserVoucher, UserVoucherId> repository) {
        super(repository);
    }
}
