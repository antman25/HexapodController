/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hexapod;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author ANTMAN
 */
public class HexapodPainter extends JPanel {
    
    private Ellipse2D jointCoxa;
    private Ellipse2D jointFemur;
    private Ellipse2D jointTibia;
    
    
    private Point2D pointWall;
    private Point2D pointCoxa;
    private Point2D pointTibia;
    private Point2D pointFemur;
    
    private Line2D segmentWallToCoxa;
    private Line2D segmentCoxaToFemur;
    private Line2D segmentFemurToTars;
    
    private Float angleFemur;
    private Float angleTibia;
    
    private final float COXA_LENGTH = 29.0F;
    private final float FEMUR_LENGTH = 76.0F;
    private final float TIBIA_LENGTH = 106.0F;
    private final int offsetWall = 125;
    private final int max_grid = 400;
            
    private final float PI_OVER_180 = (float)Math.PI / 180.0F;
    
    private Integer lowestHeight = 60;
    
    
    
    
    public void setFemurAngle(Float angle)
    {
        this.angleFemur = angle;
    }
    
    public void setTibiaAngle(Float angle)
    {
        this.angleTibia = angle;
    }
    
    public Float getFemurAngle()
    {
        return angleFemur;
    }
    
    public Float getTibiaAngle()
    {
        return angleTibia;
    }
    
    public HexapodPainter()
    {
        initSurface();
    }
    
    private void doDrawing(Graphics g) 
    {   
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g);

        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);
        
        g2d.setColor(new Color(255, 255, 255));
        
        
        
        g2d.drawLine (offsetWall,0,offsetWall,max_grid);
        g2d.drawLine (0,offsetWall,max_grid,offsetWall);
        g2d.setColor(new Color(255, 255, 0));
        g2d.drawLine (0,offsetWall+lowestHeight,max_grid,offsetWall+lowestHeight);
        
        for (int i = -20;i < 30;i++)
        {
            if (i % 10 == 0)
            {
                if (i != 0)
                {
                    g2d.setColor(new Color(128, 128, 0));
                    g2d.drawLine(offsetWall + i*10, 0,offsetWall + i*10,  400);
                }
            }
            else if (i % 2 == 0)
            {
                g2d.setColor(new Color(255, 255, 255));
                g2d.drawLine(offsetWall + i*10, offsetWall-4,offsetWall + i*10,  offsetWall +4);
                g2d.setColor(new Color(0, 128, 128));
                g2d.drawLine(offsetWall + i*10, 0,offsetWall + i*10,  400);
            }
            else
            {
                g2d.setColor(new Color(255, 255, 255));
                g2d.drawLine(offsetWall + i*10,  offsetWall-2,offsetWall + i*10,   offsetWall +2);
            }
        }

        for (int i = -20;i < 30;i++)
        {
            if (i % 2 == 0)
            {
                g2d.setColor(new Color(255, 255, 255));
                g2d.drawLine(offsetWall-4,  offsetWall - i*10,offsetWall+4,   offsetWall - i*10);
                g2d.setColor(new Color(0, 128, 128));
                g2d.drawLine(offsetWall,  offsetWall - i*10,offsetWall+400,   offsetWall - i*10);
            }
            else
            {
                g2d.setColor(new Color(255, 255, 255));
                g2d.drawLine(offsetWall-2,  offsetWall - i*10,offsetWall+2,   offsetWall - i*10);
            }
        }
        
        Point2D pointDrawCoxa = convertPoint((float)pointCoxa.getX(),(float)pointCoxa.getY());
        Point2D pointDrawFemur = convertPoint((float)pointFemur.getX(),(float)pointFemur.getY());
        Point2D pointDrawTibia = convertPoint((float)pointTibia.getX(),(float)pointTibia.getY());
        
        g2d.setColor(new Color(255, 0, 0));
        g2d.drawOval((int)pointDrawCoxa.getX() - 10,(int)pointDrawCoxa.getY() - 10, 20,20);
        g2d.drawLine((int)segmentWallToCoxa.getX1(),(int)segmentWallToCoxa.getY1(),(int)segmentWallToCoxa.getX2(), (int)segmentWallToCoxa.getY2());
        
        g2d.setColor(new Color(0, 255, 0));
        g2d.drawLine((int)segmentCoxaToFemur.getX1(),(int)segmentCoxaToFemur.getY1(),(int)segmentCoxaToFemur.getX2(), (int)segmentCoxaToFemur.getY2());
        g2d.drawOval((int)pointDrawFemur.getX() - 10,(int)pointDrawFemur.getY() - 10, 20,20);
        
        g2d.setColor(new Color(0, 0, 255));
        g2d.drawLine((int)segmentFemurToTars.getX1(),(int)segmentFemurToTars.getY1(),(int)segmentFemurToTars.getX2(), (int)segmentFemurToTars.getY2());
        g2d.drawOval((int)pointDrawTibia.getX() - 10,(int)pointDrawTibia.getY() - 10, 20,20);
    }
    
    public void setCoxaOffset(Integer offset)
    {
        this.lowestHeight = offset;
        pointWall = new Point2D.Float(0,0);
        setAngles(angleFemur,angleTibia);
    }
    
    public void setAngles(Float angleFemur, Float angleTibia)
    {
        this.angleFemur = angleFemur;
        this.angleTibia = angleTibia;
        
        //angleFemur = -angleFemur + 90;
        
        float x=COXA_LENGTH;
        float y=0;
        pointCoxa = new Point2D.Float(x, y);
        //System.out.println("Coxa: [" + Double.toString(pointCoxa.getX()) + " , " + Double.toString(pointCoxa.getY()) + "] ");
        
        
        x += (float)(FEMUR_LENGTH*Math.cos((double)((angleFemur) * PI_OVER_180)));
        y += (float)(FEMUR_LENGTH*Math.sin((double)((angleFemur)* PI_OVER_180)));
        pointFemur = new Point2D.Float(x ,y);
        System.out.println("Femur- X:" + Float.toString(x) + " Y: " + Float.toString(y) + "] ");
        
        x += (float)(TIBIA_LENGTH*Math.cos((double)((angleFemur + angleTibia + 90) * PI_OVER_180)));   
        y +=  (float)(TIBIA_LENGTH*Math.sin((double)((angleFemur + angleTibia +90 )* PI_OVER_180)));
        pointTibia = new Point2D.Float(x,y);
        System.out.println("Tibia- X:" + Float.toString(x) + " Y: " + Float.toString(y) + "] ");
        
        Point2D transWall = convertPoint(pointWall);
        Point2D transCoxa = convertPoint(pointCoxa);
        Point2D transFemur = convertPoint(pointFemur);
        Point2D transTibia = convertPoint(pointTibia);
        
        
        segmentWallToCoxa = new Line2D.Float(transWall,transCoxa);
        segmentCoxaToFemur = new Line2D.Float(transCoxa,transFemur);
        segmentFemurToTars = new Line2D.Float(transFemur,transTibia);
        
        jointCoxa = new Ellipse2D.Float((float)transCoxa.getX() - 10.0F,(float)transCoxa.getY() - 10.0F, 20.0F,20.0F);
        jointFemur = new Ellipse2D.Float((float)transFemur.getX() - 10.0F,(float)transFemur.getY() - 10.0F, 20.0F,20.0F);
        jointTibia = new Ellipse2D.Float((float)transTibia.getX() - 10.0F,(float)transTibia.getY() - 10.0F, 20.0F,20.0F);
        
        repaint();
    }
    
    private void initSurface() {
        
        this.addMouseListener(new HitTestAdapter());

        /*rect = new Rectangle2D.Float(20f, 20f, 80f, 50f);
        ellipse = new Ellipse2D.Float(120f, 30f, 60f, 60f);

        alpha_rectangle = 1f;
        alpha_ellipse = 1f;*/
        
        pointWall = new Point2D.Float(0,lowestHeight);
        setAngles(0F, 0F);
    }
    
    Point2D convertPoint(float x, float y)
    {       
        float transX = (float)x + offsetWall;
        float transY = (float)y + offsetWall;
        
        Point2D result = new Point2D.Float(transX, transY);
        return result;
    }
    
    Point2D convertPoint(Point2D point)
    {
        return convertPoint((float)point.getX(),(float)point.getY());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 400);
    }
    
    


    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }
    
    /*class RectRunnable implements Runnable {

        private Thread runner;

        public RectRunnable() {
            
            initThread();
        }
        
        private void initThread() {
            
            runner = new Thread(this);
            runner.start();
        }

        @Override
        public void run() {

            while (alpha_rectangle >= 0) {
                
                repaint();
                alpha_rectangle += -0.01f;

                if (alpha_rectangle < 0) {
                    alpha_rectangle = 0;
                }

                try {
                    
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    
                     Logger.getLogger(HexapodPainter.class.getName()).log(Level.SEVERE, 
                             null, ex);
                }
            }
        }
    }*/

    class HitTestAdapter extends MouseAdapter
            implements Runnable {

        //private RectRunnable rectAnimator;
        //private Thread ellipseAnimator;

        @Override
        public void mousePressed(MouseEvent e) {
            
            int x = e.getX();
            int y = e.getY();

            System.out.println("X: " + Integer.toString(x)+ " Y: " + Integer.toString(y));
            
            if (jointCoxa.contains(x,y))
            {
                System.out.println("Coxa Hit");
            }
            
            if (jointFemur.contains(x,y))
            {
                System.out.println("Femur Hit");
            }
            
            if (jointTibia.contains(x,y))
            {
                System.out.println("Tibia Hit");
            }
            
            /*if (rect.contains(x, y)) {

                rectAnimator = new RectRunnable();
            }

            if (ellipse.contains(x, y)) {

                ellipseAnimator = new Thread(this);
                ellipseAnimator.start();
            }*/
        }

        @Override
        public void run() {

            /*while (alpha_ellipse >= 0) {

                repaint();
                alpha_ellipse += -0.01f;

                if (alpha_ellipse < 0) {

                    alpha_ellipse = 0;
                }

                try {
                    
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    
                    Logger.getLogger(HexapodPainter.class.getName()).log(Level.SEVERE, 
                        null, ex);
                }
            }*/
        }
    }
}
