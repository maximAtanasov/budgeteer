package org.wickedsource.budgeteer.persistence.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * (dynamic) Fields that could be used by a contract associated with a project
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="PROJECT_CONTRACT_FIELD", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"FIELD_NAME", "PROJECT_ID"})
})
public class ProjectContractField implements Serializable{
    @Id
    @GeneratedValue
    private long id;

    @Column(name="FIELD_NAME", nullable = false, length = 255)
    private String fieldName;

    @ManyToOne
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private ProjectEntity project;

    public ProjectContractField(String fieldName, ProjectEntity projectEntity){
        this.fieldName = fieldName;
        this.project=projectEntity;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectContractField)) return false;

        ProjectContractField that = (ProjectContractField) o;

        if (id != that.id) return false;
        if (!fieldName.equals(that.fieldName)) return false;
        if (!(project.getId() == that.project.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + fieldName.hashCode();
        result = 31 * result + (int) project.getId();
        return result;
    }


    @Override
    public String toString() {
        return "ProjectContractField{" +
                "id=" + id +
                ", fieldName='" + fieldName + '\'' +
                ", project=" + project.getId() + " - " + project.getName() +
                '}';
    }
}
