package fi.csc.emrex.smp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by jpentika on 08/10/15.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SessionData {

  private String elmoSessionId;
  private String sessionId;
  private String returnUrl;
  private String url;
  private String ncpPublicKey;
}
