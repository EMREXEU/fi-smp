package fi.csc.emrex.smp.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by marko.hollanti on 20/08/15.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ElmoDocument {

  private String personName;
  private String institutionName;
  private String birthday;
  private List<ElmoResult> results;
}
