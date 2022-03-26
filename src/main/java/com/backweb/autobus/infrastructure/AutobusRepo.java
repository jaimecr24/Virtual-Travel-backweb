package com.backweb.autobus.infrastructure;

import com.backweb.autobus.domain.Autobus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutobusRepo extends JpaRepository<Autobus,Long> {
}
