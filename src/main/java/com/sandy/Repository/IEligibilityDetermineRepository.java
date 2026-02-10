package com.sandy.Repository;

import com.sandy.Entity.EligibilityDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEligibilityDetermineRepository extends JpaRepository<EligibilityDetailsEntity,Integer> {

}
