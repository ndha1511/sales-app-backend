package com.code.salesappbackend.repositories;

import com.code.salesappbackend.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends BaseRepository<Token, Long> {
}