package mops.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@Data
@Table("dozent")
public class DozentDto {

  @Id
  Long id;
  String username;
  String vorname;
  String nachname;
  String anrede;

  public static DozentDto create(String username, String vorname, String nachname, String anrede) {
    return new DozentDto(null, username, vorname, nachname, anrede);
  }

}