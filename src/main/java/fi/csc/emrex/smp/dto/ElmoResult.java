package fi.csc.emrex.smp.dto;

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
public class ElmoResult {

  private String code;
  private String name;
  private String result;
  private String credits;
  private String level;
  private String type;
  private String date;
}
