package application;

import com.kuka.common.ThreadUtil;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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

// No Wrench or CartesianVelocity imports needed

public class SliderApplication extends RoboticsAPIApplication {

    @Inject
    private LBR lbr;

    @Inject
    private ITaskLogger logger;

    private PrintWriter fileLogger;

    // --- Class members for velocity estimation ---
    private Frame lastFrame;
    private long lastTimestamp;
    private static final int VELOCITY_ESTIMATION_PERIOD_MS = 20;

    // (Your other member variables remain the same)
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
    private static final double FORCE_FIELD_STRENGTH_K = 50.0;

    @Override
    public void initialize() {
        // (Your initialize method remains the same)
        super.initialize();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Istanbul"));
            String timeStamp = sdf.format(new Date());
            String dynamicLogFileName = "C:/KRC/Roboter/log/SliderData_" + timeStamp + ".csv";
            fileLogger = new PrintWriter(new BufferedWriter(new FileWriter(dynamicLogFileName, false)));
            
            fileLogger.println("Timestamp,EndEffector_X,EndEffector_Y,EndEffector_Z,J1_Pos_rad,J2_Pos_rad,J3_Pos_rad,J4_Pos_rad,J5_Pos_rad,J6_Pos_rad,J7_Pos_rad,J1_Torque_Nm,J2_Torque_Nm,J3_Torque_Nm,J4_Torque_Nm,J5_Torque_Nm,J6_Torque_Nm,J7_Torque_Nm");
            logger.info("Log file ready to be written at: " + dynamicLogFileName);
        } catch (IOException e) {
            logger.error("Failed to open log file. Please check path and permissions. Error: " + e.getMessage(), e);
            fileLogger = null;
        }
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
        // (Initial movements remain the same)
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

        // Initialize velocity estimation variables
        lastFrame = lbr.getCurrentCartesianPosition(lbr.getFlange());
        lastTimestamp = System.nanoTime();
        
        logger.info("Engaging slider mode with ACTIVE FORCE FIELD.");

        while (!Thread.currentThread().isInterrupted()) {

            // --- Velocity Estimation ---
            Frame currentFrame = lbr.getCurrentCartesianPosition(lbr.getFlange());
            long currentTimestamp = System.nanoTime();
            double deltaTime = (currentTimestamp - lastTimestamp) / 1e9;
            
            double vel_x = 0;
            double vel_y = 0;
            
            if (deltaTime > 0) {
                double dx = currentFrame.getX() - lastFrame.getX();
                double dy = currentFrame.getY() - lastFrame.getY();
                vel_x = (dx / 1000.0) / deltaTime;
                vel_y = (dy / 1000.0) / deltaTime;
            }

            lastFrame = currentFrame;
            lastTimestamp = currentTimestamp;

            // --- Data Logging ---
            // (Your logging code remains here, unchanged)
            if (fileLogger != null) {
                JointPosition currentJoints = lbr.getCurrentJointPosition();
                TorqueSensorData currentTorques = lbr.getMeasuredTorque();
                double[] jointPos = currentJoints.get();
                double[] torqueVals = currentTorques.getTorqueValues();
                String csvLine = String.format("%d,%.2f,%.2f,%.2f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f", System.currentTimeMillis(), currentFrame.getX(), currentFrame.getY(), currentFrame.getZ(), jointPos[0], jointPos[1], jointPos[2], jointPos[3], jointPos[4], jointPos[5], jointPos[6], torqueVals[0], torqueVals[1], torqueVals[2], torqueVals[3], torqueVals[4], torqueVals[5], torqueVals[6]);
                fileLogger.println(csvLine);
                fileLogger.flush();
            }
            
            // --- Force Field by Motion Command ---
            double force_x = -FORCE_FIELD_STRENGTH_K * vel_y;
            double force_y =  FORCE_FIELD_STRENGTH_K * vel_x;

            double delta_x_flange = force_x / SLIDER_STIFFNESS_SLIDE_AXIS;
            double delta_y_flange = force_y / FORGIVING_STIFFNESS_X_AXIS;

            Frame constrainedTargetFrame = createFrameToHold(currentFrame);
            
            Frame forceFieldTarget = constrainedTargetFrame.copyWithRedundancy();
            forceFieldTarget.setX(forceFieldTarget.getX() + delta_x_flange);
            forceFieldTarget.setY(forceFieldTarget.getY() + delta_y_flange);

            // Command a linear motion to the new, slightly offset target
            lbr.move(lin(forceFieldTarget)
                    .setMode(sliderComplianceMode)
                    .setCartVelocity(250)
            );

            // --- CORRECTED: Use the constant to enforce a stable loop time ---
            ThreadUtil.milliSleep(VELOCITY_ESTIMATION_PERIOD_MS);
        }
    }

    @Override
    public void dispose() {
        // (Dispose method remains the same)
        logger.info("Dispose method called. Closing file logger.");
        if (fileLogger != null) {
            fileLogger.close();
        }
        super.dispose();
    }
    
    private Frame createFrameToHold(Frame currentPose) {
        // (createFrameToHold method remains the same)
        double clampedY = Math.max(P1_Y_MM, Math.min(P2_Y_MM, currentPose.getY()));
        return new Frame(worldFrame, Transformation.ofDeg(P1_X_MM, clampedY, P1_Z_MM, P1_A_DEG, P1_B_DEG, P1_C_DEG));
    }
}
