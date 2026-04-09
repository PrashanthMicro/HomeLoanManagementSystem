
package com.capgemini.homeloanmanagementapp.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Nominee {
    private String name;
    private String relation;
    private String contactNumber;
}
