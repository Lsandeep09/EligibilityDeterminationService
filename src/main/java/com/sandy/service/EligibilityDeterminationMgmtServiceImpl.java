package com.sandy.service;

import com.sandy.DTO.EligibilityDetailsOutput;
import com.sandy.Entity.*;
import com.sandy.Repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class EligibilityDeterminationMgmtServiceImpl implements IEligibiltyDeterminationMgmtService{

    @Autowired
    private IDcCaseRepository caseRepo;
    @Autowired
    private IPlanRepository planRepo;
    @Autowired
    private IDcIncomeRepository incomeRepo;
    @Autowired
    private IDcChildrenRepository childRepo;
    @Autowired
    private IApplicationRegistrationRepository citizenRepo;
    @Autowired
    private IDcEducationRepository educationRepo;
    @Autowired
    private ICOTriggerRepository triggerRepo;
    @Autowired
    private IEligibilityDetermineRepository eligiRepo;

    @Override
    public EligibilityDetailsOutput determineEligibility(Integer caseNo) {
        Integer appId = null;
        Integer planId = null;
        //get planId and appId based on caseNo
        Optional<DcCaseEntity> optCaseEntity = caseRepo.findById(caseNo);
        if(optCaseEntity.isPresent()) {
            DcCaseEntity caseEntity = optCaseEntity.get();
            planId=caseEntity.getPlanId();
            appId=caseEntity.getAppId();

        }
        //get the plan Name
        String planName = null;
        Optional<PlanEntity> optPlanEntity = planRepo.findById(planId);
        if(optPlanEntity.isPresent()) {
            PlanEntity planEntity = optPlanEntity.get();
            planName=planEntity.getPlanName();
        }

        //calculate citizen age by getting citizen DOB through appId
        Optional<CitizenAppRegistrationEntity> optCitizenEntity=citizenRepo.findById(appId);
        int citizenAge=0;
        String citizenName=null;
        if(optCitizenEntity.isPresent()) {
            CitizenAppRegistrationEntity citizenEntity = optCitizenEntity.get();
            LocalDate citizenDOB = citizenEntity.getDob();
            citizenName=citizenEntity.getFullName();
            LocalDate sysDate=LocalDate.now();
            citizenAge=Period.between(citizenDOB,sysDate).getYears();
        }


        //call helper method to plan conditions
        EligibilityDetailsOutput eligibleOutput = applyPlanConditions(caseNo,planName,citizenAge);
        //set citizen name
        eligibleOutput.setHolderName(citizenName);
        //set the citizen name
        eligibleOutput.setHolderName(citizenName);

        //save Eligibility entity object
        EligibilityDetailsEntity eligientity = new EligibilityDetailsEntity();
        BeanUtils.copyProperties(eligibleOutput,eligientity);
        eligiRepo.save(eligientity);

        //save CoTriggers object
        CoTriggersEntity triggersEntity = new CoTriggersEntity();
        triggersEntity.setCaseNo(caseNo);
        triggersEntity.setTriggerStatus("Pending");
        triggerRepo.save(triggersEntity);

        return eligibleOutput;
    }
    private EligibilityDetailsOutput applyPlanConditions(Integer caseNo, String planName,int citizenAge) {
        EligibilityDetailsOutput eligibleOutput = new EligibilityDetailsOutput();
        eligibleOutput.setPlanName(planName);

        //get income details of the citizen
        DcIncomeEntity incomeEntity=incomeRepo.findByCaseNo(caseNo);
        double empIncome = incomeEntity.getEmpIncome();
        double propertyIncome = incomeEntity.getPropertyIncome();


        //for SNAP
        if(planName.equalsIgnoreCase("SNAP")) {
            if(empIncome>300) {
                eligibleOutput.setPlanStatus("Approved");
                eligibleOutput.setBenefitAmt(200.0);
            }
            else {
                eligibleOutput.setPlanStatus(planName);
                eligibleOutput.setDenialReason("High Income");
            }
        } //for CCAP
        else if(planName.equalsIgnoreCase("CCAP")) {
            boolean kidsCountCondition = false;
            boolean kidAgeCondition = false;
            List<DcChildrenEntity> listchilds = childRepo.findByCaseNo(caseNo);
            if(listchilds.isEmpty()) {
                kidsCountCondition=true;
                for(DcChildrenEntity child:listchilds) {
                    int kidAge = Period.between(child.getChildDOB(),LocalDate.now()).getYears();
                    if(kidAge>16) {
                        kidAgeCondition = false;
                        break;
                    }//if
                }//for
            }//if
            if(empIncome<=300 && kidsCountCondition && kidAgeCondition) {
                eligibleOutput.setPlanStatus("Approved");
                eligibleOutput.setBenefitAmt(300.0);
            }
            else {
                eligibleOutput.setPlanStatus("Denied");
                eligibleOutput.setDenialReason("CCAP rules are not satisfied");
            }
        }// for medcare

        else if (planName.equalsIgnoreCase("MEDICARE")) {
            if(citizenAge>=65) {
                eligibleOutput.setPlanStatus("Approved");
                eligibleOutput.setBenefitAmt(350.0);
            }
            else{
                eligibleOutput.setPlanStatus("Denied");
                eligibleOutput.setDenialReason("MEDICARE Rules Are not Satisfied");
            }

            
        }
        else if(planName.equalsIgnoreCase("MEDAID")) {
            if(empIncome<=300 && propertyIncome==0) {
                eligibleOutput.setPlanStatus("Approved");
                eligibleOutput.setBenefitAmt(200.0);
            } else {
                eligibleOutput.setPlanStatus("Denied");
                eligibleOutput.setDenialReason("MEDAID Rules Are Not Satisfied");
            }

        }
        else if (planName.equalsIgnoreCase("CAJW")) {
            DcEducationEntity educationEntity = educationRepo.findByCaseNo(caseNo);
            int passOutYear = educationEntity.getPassOutYear();
            if(empIncome==0 && passOutYear<LocalDate.now().getYear()) {
                eligibleOutput.setPlanStatus("Approved");
                eligibleOutput.setBenefitAmt(300.0);
            }
            else {
                eligibleOutput.setPlanStatus("Denied");
                eligibleOutput.setDenialReason("CJAW rules are not satisfied");
            }


        } else if(planName.equalsIgnoreCase("QHP")){
             if(citizenAge>=1)  {
                 eligibleOutput.setPlanStatus("Approved");
             }
             else {
                 eligibleOutput.setPlanStatus("Denied");
                 eligibleOutput.setDenialReason("QHP rules are not satisfied");
             }

        }
        //set the common properties for eligibleOutput obj
        if(eligibleOutput.getPlanStatus().equalsIgnoreCase("Approved")) {
            eligibleOutput.setPlanStartDate(LocalDate.now());
            eligibleOutput.setPlanEndDate(LocalDate.now().plusYears(2));
        }
        return eligibleOutput;
    }
}
