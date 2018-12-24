package org.wickedsource.budgeteer.service.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.wickedsource.budgeteer.persistence.budget.BudgetEntity;
import org.wickedsource.budgeteer.persistence.budget.BudgetRepository;
import org.wickedsource.budgeteer.persistence.contract.ContractEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractFieldEntity;
import org.wickedsource.budgeteer.persistence.contract.ContractRepository;
import org.wickedsource.budgeteer.persistence.invoice.InvoiceRepository;
import org.wickedsource.budgeteer.persistence.project.ProjectContractField;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;
import org.wickedsource.budgeteer.web.pages.contract.overview.table.ContractOverviewTableModel;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

@Service
@Transactional
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ContractDataMapper mapper;

    @PreAuthorize("canReadProject(#projectId)")
    public ContractOverviewTableModel getContractOverviewByProject(long projectId) {
        ContractOverviewTableModel result = new ContractOverviewTableModel();
        result.setContracts(mapper.map(contractRepository.findByProjectId(projectId)));
        return result;
    }


    @PreAuthorize("canReadContract(#contractId)")
    public ContractBaseData getContractById(long contractId) {
        ContractBaseData data = mapper.map(contractRepository.findOne(contractId));
        return data;
    }

    @PreAuthorize("canReadProject(#projectId)")
    public List<ContractBaseData> getContractsByProject(long projectId) {
        List<ContractEntity> contracts = new LinkedList<>();
        contracts.addAll(contractRepository.findByProjectId(projectId));
        return mapper.map(contracts);
    }

    @PreAuthorize("canReadProject(#projectId)")
    public ContractBaseData getEmptyContractModel(long projectId) {
        ProjectEntity project = projectRepository.findOne(projectId);
        ContractBaseData model = new ContractBaseData(projectId);
        Set<ProjectContractField> fields = project.getContractFields();
        for (ProjectContractField field : fields) {
            model.getContractAttributes().add(new DynamicAttributeField(field.getFieldName(), ""));
        }
        return model;
    }

    public long save(ContractBaseData contractBaseData) {
        ProjectEntity project = projectRepository.findOne(contractBaseData.getProjectId());
        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setId(0);
        contractEntity.setProject(project);

        if (contractBaseData.getContractId() != 0) {
            contractEntity = contractRepository.findOne(contractBaseData.getContractId());
        }
        //Update basic information
        contractEntity.setName(contractBaseData.getContractName());
        contractEntity.setBudget(contractBaseData.getBudget());
        contractEntity.setInternalNumber(contractBaseData.getInternalNumber());
        contractEntity.setStartDate(contractBaseData.getStartDate());
        contractEntity.setType(contractBaseData.getType());
        contractEntity.setLink(contractBaseData.getFileModel().getLink());
        contractEntity.setFileName(contractBaseData.getFileModel().getFileName());
        contractEntity.setFile(contractBaseData.getFileModel().getFile());

        if (contractBaseData.getTaxRate() < 0) {
            throw new IllegalArgumentException("Taxrate must be positive.");
        } else {
            contractEntity.setTaxRate(new BigDecimal(contractBaseData.getTaxRate()));
        }

        //update additional information of the current contract
        for (DynamicAttributeField fields : contractBaseData.getContractAttributes()) {
            if(fields.getValue() == null){
                fields.setValue("");
            }
            boolean attributeFound = false;
            //see, if the attribute already exists -> Update the value
            for (ContractFieldEntity contractFieldEntity : contractEntity.getContractFields()) {
                if (contractFieldEntity.getField().getFieldName().equals(fields.getName().trim())) {
                    contractFieldEntity.setValue(fields.getValue());
                    attributeFound = true;
                    break;
                }
            }

            // Create a new Attribute
            if (!attributeFound) {
                // see if the Project already contains a field with this name. If not, create a new one
                ProjectContractField projectContractField = projectRepository.findContractFieldByName(contractBaseData.getProjectId(), fields.getName().trim());
                if (projectContractField == null) {
                    projectContractField = new ProjectContractField(0, fields.getName().trim(), project);
                }
                ContractFieldEntity field = new ContractFieldEntity();
                field.setId(0);
                field.setField(projectContractField);
                field.setValue(fields.getValue() == null ? "" : fields.getValue().trim());
                contractEntity.getContractFields().add(field);
            }

            //Delete any missing fields
            List <ContractFieldEntity> updatedEntities = new ArrayList<>();
            for (ContractFieldEntity contractFieldEntity : contractEntity.getContractFields()) {
                boolean found = false;
                for(DynamicAttributeField dynField : contractBaseData.getContractAttributes()){
                    if (contractFieldEntity.getField().getFieldName().equals(dynField.getName().trim())) {
                        found = true;
                    }
                }
                if(found){
                    updatedEntities.add(contractFieldEntity);
                }
            }
            contractEntity.getContractFields().removeIf(updatedEntities::contains);
        }
        contractRepository.save(contractEntity);

        return contractEntity.getId();
    }

    @PreAuthorize("canReadContract(#contractId)")
    public void deleteContract(long contractId) {
        List<BudgetEntity> budgets = budgetRepository.findByContractId(contractId);
        for (BudgetEntity budgetEntity : budgets) {
            budgetEntity.setContract(null);
        }
        budgetRepository.save(budgets);

        invoiceRepository.deleteInvoiceFieldsByContractId(contractId);
        invoiceRepository.deleteInvoicesByContractId(contractId);

        contractRepository.delete(contractId);
    }

    @PreAuthorize("canReadContract(#contractId)")
    public List<Date> getMonthList(long contractId) {
        List<Date> months = new ArrayList<>();
        ContractEntity contract = contractRepository.findById(contractId);
        Calendar cal = Calendar.getInstance();
        cal.setTime(contract.getStartDate());
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());
        while (cal.before(currentDate)) {
            months.add(cal.getTime());
            cal.add(Calendar.MONTH, 1);
        }
        return months;
    }

    @PreAuthorize("canReadProject(#projectId)")
    public List<Date> getMonthListForProjectId(long projectId) {
        List<ContractEntity> contracts = contractRepository.findByProjectId(projectId);
        Date startDate = new Date();
        for (ContractEntity contract : contracts) {
            if (contract.getStartDate().before(startDate)) {
                startDate = contract.getStartDate();
            }
        }

        List<Date> months = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());
        while (cal.before(currentDate)) {
            months.add(cal.getTime());
            cal.add(Calendar.MONTH, 1);
        }
        return months;
    }

    @PreAuthorize("canReadProject(#projectId)")
    public boolean projectHasContracts(long projectId) {
        List<ContractEntity> contracts = contractRepository.findByProjectId(projectId);
        return (null != contracts && !contracts.isEmpty());
    }

    public boolean deleteContractField(DynamicAttributeField field, long projectId) {
        for(ContractBaseData contract : getContractsByProject(projectId)){
            for(DynamicAttributeField attField : contract.getContractAttributes()){
                if(attField.getName().equals(field.getName())){
                    if(!attField.getValue().isEmpty()){
                        return false; //If a non empty field is found - do no delete anything and just return.
                    }else{
                        contract.getContractAttributes().remove(field);
                        save(contract);
                        contractRepository.deleteContractFieldByProjectIdAndFieldName(projectId, field.getName());
                        break;
                    }
                }
            }
        }
        return true;
    }
}
