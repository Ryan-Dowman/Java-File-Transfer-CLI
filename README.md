![Group 14](https://github.com/user-attachments/assets/f97cbccd-2919-4487-a67b-e982bbc41243)

# Java File Transfer CLI 

## Table of Contents
- [Introduction](#introduction)
- [Showcase](#showcase)
- [Room for Improvement](#room-for-improvement)
- [Notes](#notes)

## Introduction
Ever wanted to transfer files between devices but always find it hard to get a USB? Me too. This should hopefully provide some relief as this Java CLI allows for two devices to transfer files across a network with no middleman needed. No more sending files through emails or uploading them online first. Simply use one device to host and let your other device connect and navigate to find what they want.

## Showcase
Usually I would show off my proudly crafted UI but since this is a simple CLI I will just show off what it looks like from the perspective of the Client and the Host.

## Room for Improvement
This project actually was first a JavaFX project that included a complete UI for navigation but that idea was scrapped when I realised the project was probably better suited for a CLI. Below are some other ideas I had but did not implement:

* Allow for creating a target folder for downloads: I didn't want the client to be able to change the target folder while admist a download and since downloads act like a background process it would be annoying to attempt an action whilst being interrupted by a backlog of downloads.

* Stop the user input being interrupted by download complete messages: I tried a few ways to fix this but it was not work the effort at the moment to fix something that in essence only affects aestetics

## Notes
Make sure to use this program responsibly.
