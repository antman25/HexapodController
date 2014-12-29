/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexapod;

/**
 *
 * @author ANTMAN
 */
public class HexapodLeg {
    private float wantedX;    
    private float wantedY;
    private float wantedZ;
    
    private float angleCoxa;    
    private float angleFemur;
    private float angleTibia;
    
    private float lenCoxa;
    private float lenFemur;
    private float lenTibia;
    
    private Integer zeroCoxa;
    private Integer zeroFemur;
    private Integer zeroTibia;
    
    
    private final Integer SERVO_MAX = 2400;
    private final Integer SERVO_MIN = 600;
    
    
    public Integer AngleToTime(float angle, Integer zero)
    {
        float TimePerDeg = (SERVO_MAX - SERVO_MIN) / 180.0F;
        Integer result = (int)((angle + 90.0F) * TimePerDeg) + SERVO_MIN;
        if (result > SERVO_MAX)
            result = SERVO_MAX;
        if (result < SERVO_MIN)
            result = SERVO_MIN;
        return result;
    }
    
    
    public HexapodLeg(float lenCoxa, float lenFemur, float lenTibia)
    {
        this.lenCoxa = lenCoxa;
        this.lenFemur = lenFemur;
        this.lenTibia = lenTibia;
    }
    
    public void updateIK()
    {
        
    }
}
