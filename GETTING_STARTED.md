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

*(A paragraph describing the smartPAD as our primary interface for direct control, running applications, and diagnosing issues. Highlight the enabling switches and emergency stop button.)*

![KUKA SmartPad](assets/smartpad-menu.png)

---

## 2. Software and Development Workflow

Our development happens on a separate PC and is then transferred to the robot controller.

### KUKA Sunrise.Workbench
![Sunrise Workbench IDE](assets/sunrise_workbench.png)

*(A paragraph explaining that Sunrise.Workbench is the Eclipse-based Integrated Development Environment (IDE) we use to write, compile, and manage our Java applications.)*
