package fi.csc.emrex.smp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by marko.hollanti on 02/09/15.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NCPResult {

  private String countryCode;
  private String acronym;
  private String url;
  private String certificate;
}
