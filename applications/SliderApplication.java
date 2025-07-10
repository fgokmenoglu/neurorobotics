package application;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
// Import statement needed for TimeZone
import java.util.TimeZone;

import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.math.Transformation;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.roboticsAPI.motionModel.controlModeModel.PositionControlMode;
import com.kuka.task.ITaskLogger;
import javax.inject.Inject;
import com.kuka.roboticsAPI.deviceModel.JointPosition;
import com.kuka.roboticsAPI.sensorModel.TorqueSensorData;

public class SliderApplication extends RoboticsAPIApplication {

    @Inject
    private LBR lbr;

    @Inject
    private ITaskLogger logger;

    private PrintWriter fileLogger;

    // ... (rest of your member variables P1, P2, etc. remain the same)
    private static final double P1_X_MM = 475.0; 
    private static final double P1_Y_MM = -245.0;
    private static final double P1_Z_MM = 260.0;
    private static final double P1_A_DEG = -90.0;
    private static final double P1_B_DEG = 0.0;
    private static final double P1_C_DEG = -180.0;
    private static final double P2_Y_MM = 230.0;
    private Frame p1;
    private ObjectFrame worldFrame;
    private JointPosition safeIntermediateJointPosition;
    private static final double SLIDER_STIFFNESS_SLIDE_AXIS = 5.0;
    private static final double FORGIVING_STIFFNESS_X_AXIS = 300.0;
    private static final double RIGID_STIFFNESS_Z_AXIS = 5000.0;
    private static final double SLIDER_STIFFNESS_ROT = 300.0;
    private static final double SLIDER_DAMPING = 0.7;
    private static final double HOLD_STIFFNESS_TRANS = 5000.0;
    private static final double HOLD_STIFFNESS_ROT = 300.0;
    private static final double HOLD_DAMPING = 1.0;
    private CartesianImpedanceControlMode sliderComplianceMode;
    private CartesianImpedanceControlMode holdComplianceMode;
    private PositionControlMode firmHoldMode;

    @Override
    public void initialize() {
        super.initialize();

        // --- MODIFIED: Initialize File Logger with a unique, timestamped filename AND correct TimeZone ---
        try {
            // 1. Create a date formatter
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

            // 2. Set the formatter's TimeZone to your local time zone
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
            
            // 3. Create the timestamp string using the configured formatter
            String timeStamp = sdf.format(new Date());
            
            // 4. Construct the full, dynamic filename
            String dynamicLogFileName = "C:/KRC/Roboter/log/SliderData_" + timeStamp + ".csv";

            // 5. Initialize the writer with the new dynamic filename
            fileLogger = new PrintWriter(new BufferedWriter(new FileWriter(dynamicLogFileName, false)));
            
            // Write the header row to the new CSV file
            fileLogger.println("Timestamp,EndEffector_X,EndEffector_Y,EndEffector_Z,J1_Pos_rad,J2_Pos_rad,J3_Pos_rad,J4_Pos_rad,J5_Pos_rad,J6_Pos_rad,J7_Pos_rad,J1_Torque_Nm,J2_Torque_Nm,J3_Torque_Nm,J4_Torque_Nm,J5_Torque_Nm,J6_Torque_Nm,J7_Torque_Nm");
            logger.info("Log file ready to be written at: " + dynamicLogFileName);
        } catch (IOException e) {
            logger.error("Failed to open log file. Please check path and permissions. Error: " + e.getMessage(), e);
            fileLogger = null;
        }
        
        // ... (The rest of your initialize method remains exactly the same)
        worldFrame = lbr.getRootFrame();
        p1 = new Frame(worldFrame, Transformation.ofDeg(P1_X_MM, P1_Y_MM, P1_Z_MM, P1_A_DEG, P1_B_DEG, P1_C_DEG));
        if (Math.abs(P1_Y_MM - P2_Y_MM) < 0.1) {
            logger.warn("CRITICAL WARNING: P1_Y_MM and P2_Y_MM are (nearly) identical. No Y-axis sliding range.");
        }
        safeIntermediateJointPosition = new JointPosition(Math.toRadians(0), Math.toRadians(30), Math.toRadians(0), Math.toRadians(-60), Math.toRadians(0), Math.toRadians(90), Math.toRadians(0));
        sliderComplianceMode = new CartesianImpedanceControlMode();
        sliderComplianceMode.parametrize(CartDOF.X).setStiffness(SLIDER_STIFFNESS_SLIDE_AXIS);
        sliderComplianceMode.parametrize(CartDOF.Y).setStiffness(FORGIVING_STIFFNESS_X_AXIS);
        sliderComplianceMode.parametrize(CartDOF.Z).setStiffness(RIGID_STIFFNESS_Z_AXIS);
        sliderComplianceMode.parametrize(CartDOF.ROT).setStiffness(SLIDER_STIFFNESS_ROT);
        sliderComplianceMode.parametrize(CartDOF.ALL).setDamping(SLIDER_DAMPING);
        holdComplianceMode = new CartesianImpedanceControlMode();
        holdComplianceMode.parametrize(CartDOF.X, CartDOF.Y, CartDOF.Z).setStiffness(HOLD_STIFFNESS_TRANS);
        holdComplianceMode.parametrize(CartDOF.ROT).setStiffness(HOLD_STIFFNESS_ROT);
        holdComplianceMode.parametrize(CartDOF.ALL).setDamping(HOLD_DAMPING);
        firmHoldMode = new PositionControlMode();
        logger.info("SliderApplication initialized for Y-AXIS SLIDING (World Frame).");
        logger.info("Set forgiving stiffness on World X-axis to: " + FORGIVING_STIFFNESS_X_AXIS + " N/m.");
    }

    @Override
    public void run() {
        // This method remains completely unchanged.
        logger.info("Moving to HOME position.");
        lbr.move(ptpHome().setMode(firmHoldMode));
        try {
            logger.info("Moving to safe intermediate joint position.");
            lbr.move(ptp(safeIntermediateJointPosition).setJointVelocityRel(0.25).setMode(firmHoldMode));
            logger.info("Reached safe intermediate joint position.");
            logger.info("Moving to slider start point P1.");
            lbr.move(ptp(p1).setJointVelocityRel(0.5).setMode(firmHoldMode));
            logger.info("Reached slider start point P1.");
        } catch (Exception e) {
            logger.error("Failed to complete initial positioning sequence: " + e.getMessage());
            return;
        }

        logger.info("Engaging slider mode and starting data logging...");

        while (!Thread.currentThread().isInterrupted()) {
            Frame currentFrame = lbr.getCurrentCartesianPosition(lbr.getFlange());
            JointPosition currentJoints = lbr.getCurrentJointPosition();
            TorqueSensorData currentTorques = lbr.getMeasuredTorque();

            logger.info(String.format("EndEffector: [X=%.1f, Y=%.1f, Z=%.1f] | JointPos: %s | JointTorque: %s", currentFrame.getX(), currentFrame.getY(), currentFrame.getZ(), currentJoints.toString(), currentTorques.toString()));

            if (fileLogger != null) {
                double[] jointPos = currentJoints.get();
                double[] torqueVals = currentTorques.getTorqueValues();

                String csvLine = String.format("%d,%.2f,%.2f,%.2f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f", System.currentTimeMillis(), currentFrame.getX(), currentFrame.getY(), currentFrame.getZ(), jointPos[0], jointPos[1], jointPos[2], jointPos[3], jointPos[4], jointPos[5], jointPos[6], torqueVals[0], torqueVals[1], torqueVals[2], torqueVals[3], torqueVals[4], torqueVals[5], torqueVals[6]);
                fileLogger.println(csvLine);
                
                fileLogger.flush(); 
            }

            Frame constrainedTargetFrame = createFrameToHold(currentFrame);
            lbr.move(lin(constrainedTargetFrame).setMode(sliderComplianceMode).setCartVelocity(150));
        }
    }

    @Override
    public void dispose() {
        // This method remains completely unchanged.
        logger.info("Dispose method called. Closing file logger.");
        if (fileLogger != null) {
            fileLogger.close();
        }
        super.dispose();
    }
    
    private Frame createFrameToHold(Frame currentPose) {
        // This method remains completely unchanged.
        double clampedY = Math.max(P1_Y_MM, Math.min(P2_Y_MM, currentPose.getY()));
        return new Frame(worldFrame, Transformation.ofDeg(P1_X_MM, clampedY, P1_Z_MM, P1_A_DEG, P1_B_DEG, P1_C_DEG));
    }
}
