package com.minhthien.hoser_backend.repository;

import com.minhthien.hoser_backend.entity.Wallet;
import com.minhthien.hoser_backend.enums.WalletOwnerType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findFirstByOwnerTypeOrderByIdAsc(WalletOwnerType ownerType);

    boolean existsByOwnerType(WalletOwnerType ownerType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.user.id = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.ownerType = :ownerType order by w.id asc")
    List<Wallet> findByOwnerTypeForUpdate(@Param("ownerType") WalletOwnerType ownerType);
}
