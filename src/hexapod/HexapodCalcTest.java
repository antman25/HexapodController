/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexapod;

import communication.TCPClient;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.DefaultListModel;

/**
 *
 * @author ANTMAN
 */
public class HexapodCalcTest extends javax.swing.JFrame {

    private final float COXA_LENGTH = 29.0F;
    private final float FEMUR_LENGTH = 76.0F;
    private final float TIBIA_LENGTH = 106.0F;
    
    private final float DEG_TO_RAD = (float)Math.PI / 180.0F;
    private final float RAD_TO_DEG = 180.0F / (float)Math.PI;
    
    private final Integer SERVO_MAX = 2400;
    private final Integer SERVO_MIN = 600;
    
    TCPClient hexapod;
    Thread threadHexapod;
    HexapodLeg leg;
    
    DefaultListModel modelLog;
    
    public void Initialize()
    {
        modelLog = new DefaultListModel();
        listLog.setModel(modelLog);
        hexapod = new TCPClient("10.0.0.100",5555);
        threadHexapod = new Thread(hexapod);
        addLog("Hexapod Thread Started");
        threadHexapod.start();
        spinnerCoxa.setValue(1500);
        spinnerFemur.setValue(1500);
        spinnerTibia.setValue(1500);
        spinnerDrawCoxaOffset.setValue(70);
        
        //HexapodPainter painter = new HexapodPainter();
        //panelLeg.add("Center", painter);
        leg = new HexapodLeg((double)COXA_LENGTH,(double)FEMUR_LENGTH,(double)TIBIA_LENGTH,0,0,0);
    }
    
    void addLog(String data)
    {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        modelLog.addElement(dateFormat.format(date)+ " - " + data);
    }
    
    private void CalcIK()
    {
        Double XVal = Double.parseDouble(textXVal.getText());
        Double YVal = Double.parseDouble(textYVal.getText());
        Double ZVal = Double.parseDouble(textZVal.getText());
        leg.CalculateIK(XVal, YVal, ZVal);
        
        Double dist = Math.sqrt((XVal - (double)COXA_LENGTH) * (XVal - (double)COXA_LENGTH) + (YVal * YVal));
        if (dist > (TIBIA_LENGTH + FEMUR_LENGTH))
        {
            System.out.println("NO SOLUTION POSSIBLE");
            return;
        }
        
        Double LegLength = Math.sqrt(XVal*XVal + ZVal*ZVal);
        System.out.println("LegLength = " + LegLength.toString());
        
        
        Double lc = LegLength - COXA_LENGTH; 
        Double HF = Math.sqrt((lc*lc) + (YVal*YVal));
        System.out.println("HF = " + HF.toString());
        Double A1 = Math.atan(lc / YVal) * RAD_TO_DEG;
        System.out.println("A1 = " + A1.toString());
        Double A2 = Math.acos( ((TIBIA_LENGTH*TIBIA_LENGTH) - (FEMUR_LENGTH*FEMUR_LENGTH) - (HF*HF)) / ( -2.0 * FEMUR_LENGTH * HF )) * RAD_TO_DEG;
        System.out.println("A2 = " + A2.toString());
        
        Double FemurAngle = (A1 + A2);
        
        Double B1 = Math.acos( ((HF*HF) - (TIBIA_LENGTH*TIBIA_LENGTH) - (FEMUR_LENGTH*FEMUR_LENGTH)) / ( -2.0 * FEMUR_LENGTH * TIBIA_LENGTH ))* RAD_TO_DEG;
        System.out.println("B1 = " + B1.toString());
        Double TibiaAngleServo = -(90 - B1) - 90;
        Double TibiaAngle = TibiaAngleServo;
        
        Double CoxaAngle = Math.atan(ZVal / XVal) * RAD_TO_DEG;
        System.out.println("IKFemur = " + FemurAngle.toString() + " IKFemurServo: " + Double.toString(-FemurAngle+90.0));
        System.out.println("IKTibia = " + TibiaAngle.toString()+ " IKTibiaServo: " + Double.toString(TibiaAngle+90.0));
        System.out.println("IKCoxa = " + CoxaAngle.toString()+ " IKCoxaServo: " + Double.toString(FemurAngle+90.0));
        
        /*Double X = (double)COXA_LENGTH;
        Double Y = 0.0;
        
        System.out.println("X1: " + X.toString());
        X += FEMUR_LENGTH*Math.sin(FemurAngle * DEG_TO_RAD);
        Y += FEMUR_LENGTH*Math.cos(FemurAngle * DEG_TO_RAD);
        
        System.out.println("X2: " + X.toString());
        X+= TIBIA_LENGTH*Math.sin((TibiaAngle +FemurAngle)* DEG_TO_RAD);
        Y+= TIBIA_LENGTH*Math.cos((TibiaAngle +FemurAngle)* DEG_TO_RAD);
        System.out.println("FinalX: " + X.toString());
        System.out.println("FinalY: " + Y.toString());*/
        
        Float f = new Float (-FemurAngle+90.0);
        Float t = new Float(TibiaAngle+90.0);
        
        hexapodPainter.setAngles(f, t);
    }
    
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
    
    private void setLegTimes(Integer chanCoxa, Integer timeCoxa, Integer chanFemur,Integer timeFemur, Integer chanTibia,Integer timeTibia)
    {
        if (timeCoxa > SERVO_MAX)
            timeCoxa = SERVO_MAX;
        if (timeCoxa < SERVO_MIN)
            timeCoxa = SERVO_MIN;
        
        
        if (timeFemur > SERVO_MAX)
            timeFemur = SERVO_MAX;
        if (timeFemur < SERVO_MIN)
            timeFemur = SERVO_MIN;
        
        if (timeTibia > SERVO_MAX)
            timeTibia = SERVO_MAX;
        if (timeTibia < SERVO_MIN)
            timeTibia = SERVO_MIN;
        
        String output = "S #" + chanCoxa.toString() + " P" + timeCoxa.toString() + " #" + chanFemur.toString() + " P" + timeFemur.toString() + " #" + chanTibia.toString() + " P" + timeTibia.toString() + " T200\r\n";
        hexapod.sendString(output);
    }
    
    private void updateAngles()
    {
        Integer angleFemur = (Integer)spinnerDrawFemurAngle.getValue();
        Integer angleTibia = (Integer)spinnerDrawTibiaAngle.getValue();
        Integer offsetCoxa = (Integer)spinnerDrawCoxaOffset.getValue();
        hexapodPainter.setCoxaOffset(offsetCoxa);
        hexapodPainter.setAngles((float)angleFemur, (float)angleTibia);
    }
    
    public HexapodCalcTest() {
        initComponents();
        Initialize();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonConnect = new javax.swing.JButton();
        panelConvert = new javax.swing.JPanel();
        buttonConvert = new javax.swing.JButton();
        textTime = new javax.swing.JTextField();
        textAngle = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listLog = new javax.swing.JList();
        textCommand = new javax.swing.JTextField();
        buttonSendCmd = new javax.swing.JButton();
        buttonDisconnect = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        spinnerCoxa = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        spinnerFemur = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        spinnerTibia = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        buttonSetTime = new javax.swing.JButton();
        checkTimeRR = new javax.swing.JCheckBox();
        checkTimeLR = new javax.swing.JCheckBox();
        checkTimeLM = new javax.swing.JCheckBox();
        checkTimeRM = new javax.swing.JCheckBox();
        checkTimeRF = new javax.swing.JCheckBox();
        checkTimeLF = new javax.swing.JCheckBox();
        hexapodPainter = new hexapod.HexapodPainter();
        jPanel2 = new javax.swing.JPanel();
        spinnerDrawFemurAngle = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        spinnerDrawTibiaAngle = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        spinnerDrawCoxaOffset = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        textXVal = new javax.swing.JTextField();
        textYVal = new javax.swing.JTextField();
        buttonCalcIK = new javax.swing.JButton();
        textZVal = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        buttonConnect.setText("Connect");
        buttonConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConnectActionPerformed(evt);
            }
        });

        panelConvert.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelConvert.setName(""); // NOI18N

        buttonConvert.setText("Calc");
        buttonConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConvertActionPerformed(evt);
            }
        });

        textTime.setEditable(false);

        jLabel1.setText("Angle");

        jLabel2.setText("Servo");

        javax.swing.GroupLayout panelConvertLayout = new javax.swing.GroupLayout(panelConvert);
        panelConvert.setLayout(panelConvertLayout);
        panelConvertLayout.setHorizontalGroup(
            panelConvertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConvertLayout.createSequentialGroup()
                .addGroup(panelConvertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelConvertLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConvertLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(panelConvertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonConvert, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textAngle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textTime, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        panelConvertLayout.setVerticalGroup(
            panelConvertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConvertLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(panelConvertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textAngle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelConvertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonConvert))
        );

        jScrollPane1.setViewportView(listLog);

        buttonSendCmd.setText("Send");
        buttonSendCmd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSendCmdActionPerformed(evt);
            }
        });

        buttonDisconnect.setText("Disconnect");
        buttonDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDisconnectActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        spinnerCoxa.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerCoxaStateChanged(evt);
            }
        });

        jLabel3.setText("Coxa");

        jLabel4.setText("Femur");

        jLabel5.setText("Tibia");

        buttonSetTime.setText("Set");
        buttonSetTime.setActionCommand("Set");
        buttonSetTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSetTimeActionPerformed(evt);
            }
        });

        checkTimeRR.setText("RR");

        checkTimeLR.setText("LR");

        checkTimeLM.setText("LM");

        checkTimeRM.setText("RM");

        checkTimeRF.setText("RF");

        checkTimeLF.setText("LF");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonSetTime, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(checkTimeRM)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkTimeLM))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(checkTimeRR)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkTimeLR))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(checkTimeRF)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkTimeLF)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(spinnerFemur, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                            .addComponent(spinnerCoxa, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(spinnerTibia))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinnerCoxa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinnerFemur, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spinnerTibia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkTimeRR)
                            .addComponent(checkTimeLR))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkTimeRM)
                            .addComponent(checkTimeLM))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkTimeRF)
                            .addComponent(checkTimeLF))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(buttonSetTime))
        );

        hexapodPainter.setBackground(new java.awt.Color(0, 0, 0));
        hexapodPainter.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout hexapodPainterLayout = new javax.swing.GroupLayout(hexapodPainter);
        hexapodPainter.setLayout(hexapodPainterLayout);
        hexapodPainterLayout.setHorizontalGroup(
            hexapodPainterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );
        hexapodPainterLayout.setVerticalGroup(
            hexapodPainterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        spinnerDrawFemurAngle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerDrawFemurAngleStateChanged(evt);
            }
        });

        jLabel7.setText("Femur Angle");

        spinnerDrawTibiaAngle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerDrawTibiaAngleStateChanged(evt);
            }
        });

        jLabel8.setText("Tibia Angle");

        spinnerDrawCoxaOffset.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerDrawCoxaOffsetStateChanged(evt);
            }
        });

        jLabel9.setText("Coxa Offset");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 20, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(4, 4, 4)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(spinnerDrawCoxaOffset, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                    .addComponent(spinnerDrawFemurAngle, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(spinnerDrawTibiaAngle)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 14, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinnerDrawCoxaOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinnerDrawFemurAngle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spinnerDrawTibiaAngle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel6.setText("X Val");

        jLabel10.setText("Y Val");

        buttonCalcIK.setText("Calc IK");
        buttonCalcIK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCalcIKActionPerformed(evt);
            }
        });

        jLabel11.setText("Z Val");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textXVal))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonCalcIK))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textZVal, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(textYVal))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(textXVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(textYVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textZVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonCalcIK))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addComponent(textCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 553, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSendCmd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(123, 123, 123))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(hexapodPainter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 113, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(buttonDisconnect)
                                    .addComponent(buttonConnect))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panelConvert, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelConvert, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(buttonConnect)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonDisconnect)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hexapodPainter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSendCmd, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConvertActionPerformed
        // TODO add your handling code here:
        Float angle = Float.parseFloat(textAngle.getText());
        Integer time = AngleToTime(angle,1500);
        textTime.setText(time.toString());
    }//GEN-LAST:event_buttonConvertActionPerformed

    private void buttonConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConnectActionPerformed
        // TODO add your handling code here:
        addLog("Attempting to connect to hexapod.");
        hexapod.connect();
        //if (hexapod.is)
    }//GEN-LAST:event_buttonConnectActionPerformed

    private void buttonSendCmdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSendCmdActionPerformed
        // TODO add your handling code here:
        hexapod.sendString(textCommand.getText() + "\r\n");
        addLog("Sending Command: " + textCommand.getText());
        //textCommand.setText(null);
    }//GEN-LAST:event_buttonSendCmdActionPerformed

    private void buttonSetTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSetTimeActionPerformed
        // TODO add your handling code here:
        Integer timeCoxa = (Integer)spinnerCoxa.getValue();
        Integer timeFemur = (Integer)spinnerFemur.getValue();
        Integer timeTibia = (Integer)spinnerTibia.getValue();
        //setLegTimes(timeCoxa,timeFemur,timeTibia);
        
        if (checkTimeLF.isSelected() == true)
        {
            setLegTimes(0,timeCoxa,1,timeFemur,2,timeTibia);
        }
        
        if (checkTimeLM.isSelected() == true)
        {
            setLegTimes(4,timeCoxa,5,timeFemur,6,timeTibia);
        }
        
        if (checkTimeLR.isSelected() == true)
        {
            setLegTimes(8,timeCoxa,9,timeFemur,10,timeTibia);
        }
        
        
        if (checkTimeRF.isSelected() == true)
        {
            setLegTimes(16,timeCoxa,17,timeFemur,18,timeTibia);
        }
        
        if (checkTimeRM.isSelected() == true)
        {
            setLegTimes(20,timeCoxa,21,timeFemur,22,timeTibia);
        }
        
        if (checkTimeRR.isSelected() == true)
        {
            setLegTimes(24,timeCoxa,25,timeFemur,26,timeTibia);
        }
    }//GEN-LAST:event_buttonSetTimeActionPerformed

    private void spinnerCoxaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerCoxaStateChanged
        // TODO add your handling code here:
        
    }//GEN-LAST:event_spinnerCoxaStateChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        hexapod.cleanUp();
        threadHexapod.interrupt();
    }//GEN-LAST:event_formWindowClosing

    private void buttonDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDisconnectActionPerformed
        // TODO add your handling code here:
        hexapod.cleanUp();
    }//GEN-LAST:event_buttonDisconnectActionPerformed

    private void spinnerDrawFemurAngleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerDrawFemurAngleStateChanged
        // TODO add your handling code here:
        updateAngles();
        
    }//GEN-LAST:event_spinnerDrawFemurAngleStateChanged

    private void spinnerDrawTibiaAngleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerDrawTibiaAngleStateChanged
        // TODO add your handling code here:
        updateAngles();
    }//GEN-LAST:event_spinnerDrawTibiaAngleStateChanged

    private void spinnerDrawCoxaOffsetStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerDrawCoxaOffsetStateChanged
        // TODO add your handling code here:
        updateAngles();
    }//GEN-LAST:event_spinnerDrawCoxaOffsetStateChanged

    private void buttonCalcIKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCalcIKActionPerformed
        // TODO add your handling code here:
        CalcIK();
    }//GEN-LAST:event_buttonCalcIKActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HexapodCalcTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HexapodCalcTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HexapodCalcTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HexapodCalcTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HexapodCalcTest().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCalcIK;
    private javax.swing.JButton buttonConnect;
    private javax.swing.JButton buttonConvert;
    private javax.swing.JButton buttonDisconnect;
    private javax.swing.JButton buttonSendCmd;
    private javax.swing.JButton buttonSetTime;
    private javax.swing.JCheckBox checkTimeLF;
    private javax.swing.JCheckBox checkTimeLM;
    private javax.swing.JCheckBox checkTimeLR;
    private javax.swing.JCheckBox checkTimeRF;
    private javax.swing.JCheckBox checkTimeRM;
    private javax.swing.JCheckBox checkTimeRR;
    private hexapod.HexapodPainter hexapodPainter;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList listLog;
    private javax.swing.JPanel panelConvert;
    private javax.swing.JSpinner spinnerCoxa;
    private javax.swing.JSpinner spinnerDrawCoxaOffset;
    private javax.swing.JSpinner spinnerDrawFemurAngle;
    private javax.swing.JSpinner spinnerDrawTibiaAngle;
    private javax.swing.JSpinner spinnerFemur;
    private javax.swing.JSpinner spinnerTibia;
    private javax.swing.JTextField textAngle;
    private javax.swing.JTextField textCommand;
    private javax.swing.JTextField textTime;
    private javax.swing.JTextField textXVal;
    private javax.swing.JTextField textYVal;
    private javax.swing.JTextField textZVal;
    // End of variables declaration//GEN-END:variables
}

