package com.sandy.service;

import com.sandy.DTO.EligibilityDetailsOutput;

public interface IEligibiltyDeterminationMgmtService {
    public EligibilityDetailsOutput determineEligibility(Integer caseNo);
}
