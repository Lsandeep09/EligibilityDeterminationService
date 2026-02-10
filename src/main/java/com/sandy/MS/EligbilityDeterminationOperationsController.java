package com.sandy.MS;

import com.sandy.DTO.EligibilityDetailsOutput;
import com.sandy.service.IEligibiltyDeterminationMgmtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ed-api")
public class EligbilityDeterminationOperationsController {

    @Autowired
    private IEligibiltyDeterminationMgmtService edService;
    @GetMapping("/determine/{caseNo}")
    public ResponseEntity<EligibilityDetailsOutput>  checkPlanEligibility(@PathVariable Integer caseNo) {
        //use the service
        EligibilityDetailsOutput output = edService.determineEligibility(caseNo);
        return new ResponseEntity<EligibilityDetailsOutput>(output, HttpStatus.CREATED);

    }
}
