package com.backweb.destino.infrastructure;

import com.backweb.destino.domain.Destino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinoRepo extends JpaRepository<Destino,Long> {
}
