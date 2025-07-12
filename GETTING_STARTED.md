# KUKA Robotics Lab: Getting Started Guide
This guide provides the essential information one would need to get started with the KUKA LBR iiwa platform in the lab. Its purpose is to get them familiar with the hardware, software, and development workflow.

![KUKA Roborics Laboratory](assets/lab_setup.png)

---

## 1. Hardware Overview

The setup consists of several key physical components.

### The KUKA LBR iiwa Robot Arm
<div align="justify">
The KUKA LBR iiwa 7 R800 is a 7-degree-of-freedom lightweight collaborative robot designed for sensitive manipulation tasks, featuring a 7 kg payload capacity and an 800 mm reach radius. This cobot integrates advanced joint torque sensors throughout its kinematic chain, enabling precise force and impedance control that makes it exceptionally well-suited for haptic interface applications. In research contexts, the LBR iiwa 7 R800 serves as a sophisticated haptic device capable of providing programmable force feedback to users while simultaneously capturing their input motions with high fidelity. Its inherent compliance and safety features allow for direct physical human-robot interaction without traditional safety barriers, making it ideal for studies involving teleoperation, skill transfer, rehabilitation robotics, and human-in-the-loop control systems where bidirectional force and motion information exchange between human operators and robotic systems is critical.
</div>

### The Sunrise Controller Cabinet
<div align="justify">
The KUKA Sunrise Cabinet serves as the "brain" of the LBR iiwa 7 R800, housing the primary controller that orchestrates all robot operations through a Windows-based operating system. This industrial PC platform provides the computational foundation for executing custom Java applications developed using KUKA's Sunrise.Workbench framework, allowing researchers to implement sophisticated control algorithms, sensor integration, and user interface components directly on the robot controller. The cabinet features essential connectivity options including Ethernet ports for network communication and data exchange with external systems, as well as USB ports for direct programming, data transfer, and peripheral device connections. This Windows-based architecture enables seamless integration with standard PC software tools and libraries while maintaining real-time control capabilities, making it particularly valuable for research applications where custom programming, data logging, and external system communication are fundamental requirements for experimental setups and algorithm development.
</div>

### The smartHMI (smartPAD)
<div align="justify">
The KUKA smartPAD serves as the primary human-machine interface for the LBR iiwa 7 R800, providing researchers with direct control capabilities through its intuitive touchscreen display and integrated safety features. This handheld teaching pendant allows operators to manually guide the robot, launch and monitor Java applications, access diagnostic information, and configure system parameters in real-time during experimental procedures. Critical safety elements include the three-position enabling switches located on the back of the device, which must be actively held in the middle position to permit robot motion, ensuring that any loss of operator control immediately halts all movement. Additionally, the prominent red emergency stop button on the front face provides instant system shutdown capability, cutting power to the robot drives and bringing all motion to an immediate stop when pressed. The smartPAD's combination of operational control and safety interlocks makes it indispensable for research environments where frequent manual intervention, application testing, and safety oversight are essential components of experimental protocols and system development workflows.
</div>

![KUKA SmartPad](assets/smartpad-menu.png)

---

## 2. Software Overview

The development happens on a separate host PC and is then transferred to the robot controller.

### KUKA Sunrise.Workbench
![Sunrise Workbench IDE](assets/sunrise_workbench.png)

<div align="justify">
Sunrise.Workbench is KUKA's Eclipse-based Integrated Development Environment (IDE) that serves as the primary software platform for developing, compiling, and managing Java applications for the LBR iiwa 7 R800 robot system. Built on the robust Eclipse framework, this specialized IDE provides researchers with comprehensive tools for writing custom robot control algorithms, implementing sensor interfaces, and creating sophisticated automation routines using KUKA's Sunrise.OS API libraries. The development environment offers standard IDE features including syntax highlighting, code completion, debugging capabilities, and project management tools, while also incorporating robot-specific functionalities such as application deployment, real-time monitoring, and direct communication with the robot controller. Researchers can leverage Sunrise.Workbench to compile their Java applications locally before transferring them to the robot's Windows-based controller for execution, enabling iterative development cycles where code can be tested, refined, and deployed efficiently. This integrated approach streamlines the development workflow for haptic interface applications and experimental control systems, allowing researchers to focus on algorithm implementation rather than low-level robot communication protocols.
</div>

---

## 3. Development Workflow

This section covers the essential steps from initial setup and software configuration to deploying and running your robotic applications.

### 3.1. Initial System and Software Setup

Before working with a project, the KUKA system must be powered on and properly connected to a development computer.

* Powering Up the System: Ensure the KUKA robot controller and all associated components have power.

* Software Installation: The KUKA Sunrise Workbench software must be installed on your computer to develop and manage robotics applications.

* Establishing a Network Connection: A stable computer connection is required to transfer your project to the robot controller. The development computer should be on the same network as the KUKA controller. A common setup uses an IP address like 172.31.1.147 for the controller.

### 3.2. Developing Your Application in Sunrise Workbench

Sunrise Workbench is the integrated development environment for creating KUKA robot applications.

1. Launch Sunrise Workbench to begin. The main window will open, displaying your most recent project.

2. Navigate the Project Structure using the "Package Explorer" on the left-hand side. Here you can access all project files, including:

   * Application source code (Java files).

   * Project-specific configurations and references.

3. View and Edit Code in the main editor window on the right. Selecting a file, such as SliderApplication.java, will display its contents for editing.

### 3.3. Deploying the Project to the KUKA Controller

Once your application is ready, you must deploy it to the robot controller to run it. This process is handled through project synchronization.

1. In the "Package Explorer," right-click on the project name (e.g., ExampleProject_R2).
  
2. From the context menu, select Sunrise > Synchronize Project.

3. A "Project Synchronization" window will appear. Ensure the Deploy to controller option is checked.
  
4. Click Run to begin transferring the project files to the KUKA controller and wait for the process to complete.

### 3.4. Running and Managing the Application via SmartPad

The KUKA smartPad is the handheld terminal used to operate the robot directly.

1. Selecting an Application: Once a project is deployed, its applications become available on the smartPad. You can find them in the "Applications" list.

2. Executing the Program: Choose your desired application from the list and press the "play" button on the smartPad to start it.
 
3. Stopping and Resetting:

  * Pressing the "stop" button on the smartPad will pause the currently running application.

  * It is recommended to reset an application after it has been run, especially before starting a new one.

### 3.5. Accessing and Reviewing Logged Data

If your application is programmed to save data, you can access the output files directly from your connected computer.

1. Open a file explorer and in the address bar, type the network path to the controller's Roboter folder. The address is \\172.31.1.147\krc\Roboter.

2. Navigate into the log folder. This directory contains data files generated by your applications.

3. Locate your data file. For example, a test run of the SliderApplication might create a CSV file named SliderData_20250705132722.csv.

4. Open the CSV file to view the data you specified for collection in your Java code. This can include timestamps, end-effector positions, joint positions, and torque values.
