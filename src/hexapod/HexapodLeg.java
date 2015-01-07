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
    private Double X;    
    private Double Y;
    private Double Z;
    
    private Double angleCoxa;    
    private Double angleFemur;
    private Double angleTibia;
    
    private Double lenCoxa;
    private Double lenFemur;
    private Double lenTibia;
    
    private Integer offsetCoxa;
    private Integer offsetFemur;
    private Integer offsetTibia;
    
    
    private final Integer SERVO_MAX = 2400;
    private final Integer SERVO_MIN = 600;
    
    private final Double DEG_TO_RAD = Math.PI / 180.0;
    private final Double RAD_TO_DEG = 180.0 / Math.PI;
    
    public Integer getCoxaServoPosition()
    {
        return AngleToTime(angleCoxa,offsetCoxa);
    }
    
    public Integer getFemurServoPosition()
    {
        return AngleToTime(angleFemur,offsetFemur);
    }
    
    public Integer getTibiaServoPosition()
    {
        return AngleToTime(angleTibia,offsetTibia);
    }
    
    public Integer AngleToTime(Double angle, Integer offset)
    {
        Double TimePerDeg = (SERVO_MAX - SERVO_MIN) / 180.0;
        Integer result = (int)((angle + 90.0) * TimePerDeg) + SERVO_MIN + offset;
        if (result > SERVO_MAX)
            result = SERVO_MAX;
        if (result < SERVO_MIN)
            result = SERVO_MIN;
        return result;
    }
    
    
    public HexapodLeg(Double lenCoxa, Double lenFemur, Double lenTibia, Integer offsetCoxa, Integer offsetFemur, Integer offsetTibia)
    {
        this.lenCoxa = lenCoxa;
        this.lenFemur = lenFemur;
        this.lenTibia = lenTibia;
        this.offsetCoxa = offsetCoxa;
        this.offsetFemur = offsetFemur;
        this.offsetTibia = offsetTibia;
        angleCoxa = 0.0;
        angleFemur = 0.0;
        angleTibia = 0.0;
    }
    
    public boolean CalculateIK(Double wantedX, Double wantedY, Double wantedZ)
    {
        Double dist = Math.sqrt((wantedX - (double)lenCoxa) * (wantedX - (double)lenCoxa) + (wantedY * wantedY));
        if (dist > (lenTibia + lenFemur))
        {
            System.out.println("HexapodLeg: NO SOLUTION POSSIBLE");
            return false;
        }
        
        Double LegLength = Math.sqrt(wantedX*wantedX + wantedZ*wantedZ);
        //System.out.println("LegLength = " + LegLength.toString());
        
        
        Double lc = LegLength - lenCoxa; 
        Double HF = Math.sqrt((lc*lc) + (wantedY*wantedY));
        //System.out.println("HF = " + HF.toString());
        Double A1 = Math.atan(lc / wantedY) * RAD_TO_DEG;
        //System.out.println("A1 = " + A1.toString());
        Double A2 = Math.acos( ((lenTibia*lenTibia) - (lenFemur*lenFemur) - (HF*HF)) / ( -2.0 * lenFemur * HF )) * RAD_TO_DEG;
        //System.out.println("A2 = " + A2.toString());
        
        Double FemurAngle = (A1 + A2);
        
        Double B1 = Math.acos( ((HF*HF) - (lenTibia*lenTibia) - (lenFemur*lenFemur)) / ( -2.0 * lenFemur * lenTibia ))* RAD_TO_DEG;
        //System.out.println("B1 = " + B1.toString());
        Double TibiaAngleServo = -(90 - B1) - 90;
        Double TibiaAngle = TibiaAngleServo;
        
        Double CoxaAngle = Math.atan(wantedZ / wantedX) * RAD_TO_DEG;
        System.out.println("HexapodLeg: IKCoxa = " + CoxaAngle.toString()+ " IKCoxaServo: " + Double.toString(CoxaAngle) + " --  " + AngleToTime(CoxaAngle,0).toString());
        System.out.println("HexapodLeg: IKFemur = " + FemurAngle.toString() + " IKFemurServo: " + Double.toString(-FemurAngle+90.0) + " --  " + AngleToTime(-FemurAngle+90.0,0).toString());
        System.out.println("HexapodLeg: IKTibia = " + TibiaAngle.toString()+ " IKTibiaServo: " + Double.toString(TibiaAngle+90.0) + " --  " + AngleToTime(TibiaAngle+90.0,0).toString());
        
      
        //double errorDist = Math.sqrt(  );
        
        this.angleCoxa = CoxaAngle;
        this.angleFemur = -FemurAngle+90.0;
        this.angleTibia = TibiaAngle+90.0;
        return true;
    }
    
    public void CalculateFK()
    {
        
    }
}
