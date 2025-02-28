![Group 14](https://github.com/user-attachments/assets/f97cbccd-2919-4487-a67b-e982bbc41243)

# Java File Transfer CLI 

## Table of Contents
- [Introduction](#introduction)
- [Showcase](#showcase)
- [Room for Improvement](#room-for-improvement)
- [Notes](#notes)

## Introduction
Ever wanted to transfer files between devices but always find it hard to get a USB? Me too. This should hopefully provide some relief, as this Java CLI allows two devices to transfer files across a network with no middleman needed. No more sending files through emails or uploading them online first. Simply use one device to host and let your other device connect and navigate to find what they want.

## Showcase
Usually, I would show off my proudly crafted UI, but to protect my privacy, I will outline the features rather than showcase what it looks like using the program.

* **Host:** The host gains full control over the client when the process starts. They always have the ability to boot the client currently connected, as well as simply ending the program to cut the connection. For full transparency with the client, the host is also informed of all folder navigation and file downloads.

* **Client:** The client’s role in this program is simple: request information. You have the ability to `cd` in and out of folders and request file downloads. Disconnecting the client will have no impact on the host, as they will acknowledge the disconnect and begin dropping their current connections to ensure new clients can be added. There can only be one client connected to the host, and due to this, the client will automatically attempt a reconnect while waiting.

## Room for Improvement
This project was originally a JavaFX project that included a complete UI for navigation, but that idea was scrapped when I realized the project was probably better suited for a CLI. Below are some other ideas I had but did not implement:

* **Allow for creating a target folder for downloads:** I didn't want the client to be able to change the target folder while amidst a download, and since downloads act like a background process, it would be annoying to attempt an action while being interrupted by a backlog of downloads.

* **Stop user input from being interrupted by download completion messages:** I tried a few ways to fix this, but it was not worth the effort at the moment to fix something that, in essence, only affects aesthetics.

## Notes
Make sure to use this program responsibly.
