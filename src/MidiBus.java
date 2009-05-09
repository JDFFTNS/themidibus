/**
 * Copyright (c) 2008 Severin Smith
 *
 * This file is part of a library called The MidiBus - http://www.smallbutdigital.com/themidibus.php.
 *
 * The MidiBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The MidiBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the MidiBus. If not, see <http://www.gnu.org/licenses/>.
*/

package themidibus;

import javax.sound.midi.*;
import java.util.Vector;
import java.util.Formatter;

import processing.core.PApplet;
import java.lang.reflect.Method;

/**
 * The MidiBus class provides a simple way to send and receive Midi within Processing sketches.
 * <p>
 * <h4>Typical Implementation, Simple</h4>
 * <p>
 * A typical simple Processing Midi application would begin by invoking the static method {@link #list()} to learn what devices are available. Then using that information a new MidiBus object would be instantiated with with the desired Midi input and/or output devices. The Processing sketch could then send midi via MidiBus's outgoing methods such as {@link #sendNoteOn(int channel, int pitch, int velocity)}, {@link #sendNoteOff(int channel, int pitch, int velocity)} and {@link #sendControllerChange(int channel, int number, int value)} and receive midi via the PApplet methods this package provides support for such as {@link PApplet#noteOn(int channel, int pitch, int velocity)}, {@link PApplet#noteOff(int channel, int pitch, int velocity)} and {@link PApplet#controllerChange(int channel, int number, int value)}.
 * <h4>Typical Implementation, Advanced</h4>
 * <p>
 * If you wish to build more complex Processing Midi applications you can add more input and output devices to any given instance of MidiBus via the addInput() and addOutput() methods. However it is important to understand that each MidiBus object acts like 2 Midi buses, one for input and one for output. This means, that by design, outgoing Midi messages are sent to <i>all</i> output devices connected to a given instance of MidiBus, and incomming messages from <i>all</i> input devices connected to a given instance of MidiBus are <i>merged</i> upon reception. In practice, this means that, by design, you cannot tell which of the devices connected to a given instance of MidiBus sent a particular message, nor can you send a Midi message to one particular device connected to that object. Instead, for independent reception/transmission to different <i>sets</i> of Midi devices, you can instantiate more than one MidiBus object inside your Processing sketch. Each instance of MidiBus will only send Midi messages to output devices which are connected to it and inbound Midi messages arriving at each MidiBus can be diferentiated using the the {@link PApplet} methods with the bus_name parameter.
 *
 * @version 004
 * @author Severin Smith
 * @see PApplet
 * @see MidiListener
 * @see RawMidiListener
 * @see StandardMidiListener
 * @see SimpleMidiListener
*/

public class MidiBus {
	
	String bus_name;
	
	public static final int INPUT = 1;
	public static final int OUTPUT = 2;
	
	Vector<InputDeviceContainer> input_devices;
	Vector<OutputDeviceContainer> output_devices;

	Vector<MidiListener> listeners;
	
	PApplet parent;
	
	Method eventMethod_noteOn, eventMethod_noteOff, eventMethod_controllerChange, eventMethod_rawMidi, eventMethod_midiMessage;
	Method eventMethod_noteOn_withBusName, eventMethod_noteOff_withBusName, eventMethod_controllerChange_withBusName, eventMethod_rawMidi_withBusName, eventMethod_midiMessage_withBusName;
	
	/* -- Constructors -- */
	
	/**
	 * Constructs a new MidiBus attached to the specified PApplet. No input or output Midi devices will be opened. The new MidiBus's bus_name will be generated automatically.
	 *
	 * @param parent the Processing PApplet to which this MidiBus is attached
	 * @see #addInput(int device_num)
	 * @see #addInput(String device_name)
	 * @see #addOutput(int device_num)
	 * @see #addOutput(String device_name)
	 * @see #list()
	*/
	public MidiBus(PApplet parent) {
		init(parent);
	}
	
	/**
	 * Constructs a new MidiBus attached to the specified PApplet and opens the Midi input and output devices specified by the indexes in_device_num and out_device_num. A value of -1 can be passed to in_device_num if no input Midi device is to be opened, or to out_device_num if no output Midi device is to be opened. The new MidiBus's bus_name will be generated automatically.
	 *
	 * @param parent the Processing PApplet to which this MidiBus is attached
	 * @param in_device_num the index of the Midi input device to be opened
	 * @param out_device_num the index of the Midi output device to be opened
	 * @see #addInput(int device_num)
	 * @see #addInput(String device_name)
	 * @see #addOutput(int device_num)
	 * @see #addOutput(String device_name)
	 * @see #list()
	*/
	public MidiBus(PApplet parent, int in_device_num, int out_device_num) {		
		init(parent);
		addInput(in_device_num);
		addOutput(out_device_num);
	}
	
	/**
	 * Constructs a new MidiBus attached to the specified PApplet with the specified bus_name and opens the Midi input and output devices specified by the indexes in_device_num and out_device_num. A value of -1 can be passed to in_device_num if no input Midi device is to be opened, or to out_device_num if no output Midi device is to be opened.
	 *
	 * @param parent the Processing PApplet to which this MidiBus is attached
	 * @param in_device_num the index of the Midi input device to be opened
	 * @param out_device_num the index of the Midi output device to be opened
	 * @param bus_name the String which which identifies this MidiBus
	 * @see #addInput(int device_num)
	 * @see #addInput(String device_name)
	 * @see #addOutput(int device_num)
	 * @see #addOutput(String device_name)
	 * @see #list()
	*/
	public MidiBus(PApplet parent, int in_device_num, int out_device_num, String bus_name) {		
		init(parent, bus_name);
		addInput(in_device_num);
		addOutput(out_device_num);
	}
	
	/**
	 * Constructs a new MidiBus attached to the specified PApplet with the specified bus_name. No input or output Midi devices will be opened.
	 *
	 * @param parent the Processing PApplet to which this MidiBus is attached
	 * @param bus_name the String which which identifies this MidiBus
	 * @see #addInput(int device_num)
	 * @see #addInput(String device_name)
	 * @see #addOutput(int device_num)
	 * @see #addOutput(String device_name)
	 * @see #list()
	*/
	public MidiBus(PApplet parent, String bus_name) {
		init(parent, bus_name);
	}
	
	/**
	 * Constructs a new MidiBus attached to the specified PApplet and opens the Midi input and output devices specified by the names in_device_name and out_device_name. An empty String can be passed to in_device_num if no input Midi device is to be opened, or to out_device_num if no output Midi device is to be opened. The new MidiBus's bus_name will be generated automatically.
	 * <p>
	 * If two or more Midi inputs have the same name, whichever appears first when {@link #list()} is called will be added, simlarly for two or more Midi outputs with the same name. If this behavior is problematic use {@link #MidiBus(PApplet parent, int in_device_num, int out_device_num)} instead.
	 *
	 * @param parent the Processing PApplet to which this MidiBus is attached
	 * @param in_device_name the name of the Midi input device to be opened
	 * @param out_device_name the name of the Midi output device to be opened
	 * @see #addInput(int device_num)
	 * @see #addInput(String device_name)
	 * @see #addOutput(int device_num)
	 * @see #addOutput(String device_name)
	 * @see #list()
	*/
	public MidiBus(PApplet parent, String in_device_name, String out_device_name) {
		init(parent);
		addInput(in_device_name);
		addOutput(out_device_name);
	}
	
	/**
	 * Constructs a new MidiBus attached to the specified PApplet with the specified bus_name and opens the Midi input and output devices specified by the names in_device_num and out_device_num. An empty String can be passed to in_device_num if no input Midi device is to be opened, or to out_device_num if no output Midi device is to be opened.
	 * <p>
	 * If two or more Midi inputs have the same name, whichever appears first when {@link #list()} is called will be added, simlarly for two or more Midi outputs with the same name. If this behavior is problematic use {@link #MidiBus(PApplet parent, int in_device_num, int out_device_num, String bus_name)} instead.
	 *
	 * @param parent the Processing PApplet to which this MidiBus is attached
	 * @param in_device_name the name of the Midi input device to be opened
	 * @param out_device_name the name of the Midi output device to be opened
	 * @param bus_name the String which which identifies this MidiBus
	 * @see #addInput(int device_num)
	 * @see #addInput(String device_name)
	 * @see #addOutput(int device_num)
	 * @see #addOutput(String device_name)
	 * @see #list()
	*/
	public MidiBus(PApplet parent, String in_device_name, String out_device_name, String bus_name) {
		init(parent, bus_name);
		addInput(in_device_name);
		addOutput(out_device_name);
	}
	
	/* -- Constructor Functions -- */
	
	/**
	 * Creates a new (hopefully/probably) unique bus_name value for new MidiBus objects that weren't given one and then calls the regular init() function. 
	 * If two MidiBus object were to have the same name, this would be bad, but not fatal, so there's no point in spending too much time worrying about it.
	*/
	private void init(PApplet parent) {
		String id = new Formatter().format("%08d", System.currentTimeMillis()%100000000).toString();
		init(parent, "MidiBus_"+id);
	}
	
	/**
	 * Perfoms the initialisation of new MidiBus objects, is private for a reason, and is only ever called within the constructors. This method exists only for the purpose of cleaner and easier to maintain code.
	*/
	private void init(PApplet parent, String bus_name) {
		this.parent = parent;
	
		parent.registerDispose(this);
		
		try {
			eventMethod_noteOn = parent.getClass().getMethod("noteOn", new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
	
		try {
			eventMethod_noteOff = parent.getClass().getMethod("noteOff", new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}

		try {
			eventMethod_controllerChange = parent.getClass().getMethod("controllerChange", new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
		
		try {
			eventMethod_rawMidi = parent.getClass().getMethod("rawMidi", new Class[] { byte[].class });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
		
		try {
			eventMethod_midiMessage = parent.getClass().getMethod("midiMessage", new Class[] { MidiMessage.class });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
	
		try {
			eventMethod_noteOn_withBusName = parent.getClass().getMethod("noteOn", new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
	
		try {
			eventMethod_noteOff_withBusName = parent.getClass().getMethod("noteOff", new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}

		try {
			eventMethod_controllerChange_withBusName = parent.getClass().getMethod("controllerChange", new Class[] { Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
		
		try {
			eventMethod_rawMidi_withBusName = parent.getClass().getMethod("rawMidi", new Class[] { byte[].class, String.class });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
		
		try {
			eventMethod_midiMessage_withBusName = parent.getClass().getMethod("midiMessage", new Class[] { MidiMessage.class, String.class });
		} catch (Exception e) {
			// no such method, or an error.. which is fine, just ignore
		}
		
		/* -- */
		
		this.bus_name = bus_name;
	
		/* -- */
		
		input_devices = new Vector<InputDeviceContainer>();
		output_devices = new Vector<OutputDeviceContainer>();
		
		listeners = new Vector<MidiListener>();
	}

	/* -- Input/Output Handling -- */
	
	public String[] attachedInputs() {
		MidiDevice.Info[] devices_info = attachedInputsMidiDeviceInfo();
		String[] devices = new String[devices_info.length];
		
		for(int i = 0;i < devices_info.length;i++) {
			devices[i] = devices_info[i].getName();
		}
		
		return devices;
	}
	
	public String[] attachedOutputs() {
		MidiDevice.Info[] devices_info = attachedOutputsMidiDeviceInfo();
		String[] devices = new String[devices_info.length];
		
		for(int i = 0;i < devices_info.length;i++) {
			devices[i] = devices_info[i].getName();
		}
		
		return devices;
	}
	
	MidiDevice.Info[] attachedInputsMidiDeviceInfo() {
		MidiDevice.Info[] devices = new MidiDevice.Info[input_devices.size()];
	
		for(int i = 0;i < input_devices.size();i++) {
			devices[i] = input_devices.get(i).info;
		}
		
		return devices;
	}
	
	MidiDevice.Info[] attachedOutputsMidiDeviceInfo() {
		MidiDevice.Info[] devices = new MidiDevice.Info[output_devices.size()];
	
		for(int i = 0;i < output_devices.size();i++) {
			devices[i] = output_devices.get(i).info;
		}
		
		return devices;
	}
	
	/**
	 * Adds a new Midi input device specified by the index device_num. If the Midi input device has already been added, it will not be added again.
	 *
	 * @param device_num the index of the Midi input device to be added
	 * @return true if and only if the input device was successfully added
	 * @see #addInput(String device_name)
	 * @see #list()
	*/
	public boolean addInput(int device_num) {
		if(device_num == -1) return false;

		MidiDevice.Info[] devices = availableInputsMidiDeviceInfo();
		
		if(device_num >= devices.length || device_num < 0) {
			System.err.println("\nThe MidiBus Warning: The chosen input device numbered ["+device_num+"] was not added because it doesn't exist");
			return false;
		}
		
		return addInput(devices[device_num]);
	}
	
	
	/**
	 * Removes the Midi input device specified by the index device_num.
	 *
	 * @param device_num the index of the Midi input device to be removed
	 * @return true if and only if the input device was successfully removed
	 * @see #removeInput(String device_name)
	 * @see #attachedInputs()
	*/
	public boolean removeInput(int device_num) {
		try {
			InputDeviceContainer container = input_devices.get(device_num);
		
			container.transmitter.close();
			container.receiver.close();
			container.device.close();
		
			input_devices.remove(container);
			return true;
		} catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}
	
	/**
	 * Adds a new Midi input device specified by the name device_name. If the Midi input device has already been added, it will not be added again.
	 * <p>
	 * If two or more Midi inputs have the same name, whichever appears first when {@link #list()} is called will be added. If this behavior is problematic use {@link #addInput(int device_num)} instead.
	 *
	 * @param device_name the name of the Midi input device to be added
	 * @return true if and only if the input device was successfully added
	 * @see #addInput(int device_num)
	 * @see #list()
	*/
	public boolean addInput(String device_name) {
		if(device_name.equals("")) return false;
		
		MidiDevice.Info[] devices = availableInputsMidiDeviceInfo();
		
		for(int i = 0;i < devices.length;i++) {
			if(devices[i].getName().equals(device_name)) return addOutput(devices[i]);
		}
		
		System.err.println("\nThe MidiBus Warning: No available input Midi devices named: \""+device_name+"\" were found");
		return false;
	}
	
	/**
	 * Removes the Midi input device specified by the name device_name.
	 * <p>
	 * If two or more attached Midi inputs have the same name, whichever appears first when {@link #attachedInputs()} is called will be removed. If this behavior is problematic use {@link #removeInput(int device_num)} instead.
	 *
	 * @param device_name the name of the Midi input device to be removed
	 * @return true if and only if the input device was successfully removed
	 * @see #removeInput(int device_num)
	 * @see #attachedInputs()
	*/
	public boolean removeInput(String device_name) {
		for(InputDeviceContainer container : input_devices) {
			if(container.info.getName().equals(device_name)) {
				container.transmitter.close();
				container.receiver.close();
				container.device.close();

				input_devices.remove(container);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds a new Midi input device specified by the MidiDevice.Info device_info. If the Midi input device has already been added, it will not be added again.
	 *
	 * @param device_info the MidiDevice.Info of the Midi input device to be added
	 * @return true if and only if the input device was successfully added
	*/
	boolean addInput(MidiDevice.Info device_info) {
		try {
			MidiDevice new_device = MidiSystem.getMidiDevice(device_info);
		
			if(new_device.getMaxTransmitters() == 0) {
				System.err.println("\nThe MidiBus Warning: The chosen input device \""+device_info.getName()+"\" was not added because it is output only");
				return false;
			}
			
			for(InputDeviceContainer container : input_devices) {
				if(device_info.equals(container.info)) return false;
			}

			new_device.open();

			MReceiver receiver = new MReceiver();
			Transmitter transmitter = new_device.getTransmitter();
			transmitter.setReceiver(receiver);
			
			InputDeviceContainer new_container = new InputDeviceContainer(new_device);
			new_container.transmitter = transmitter;
			new_container.receiver = receiver;
			
			input_devices.add(new_container);
			
			return true;
		} catch (MidiUnavailableException e) {
			System.err.println("\nThe MidiBus Warning: The chosen input device \""+device_info.getName()+"\" was not added because it is unavailable");
			return false;
		}
	}
	
	/**
	 * Adds a new Midi output device specified by the index device_num. If the Midi output device has already been added, it will not be added again.
	 *
	 * @param device_num the index of the Midi output device to be added
	 * @return true if and only if the output device was successfully added
	 * @see #addOutput(String device_name)
	 * @see #list()
	*/
	public boolean addOutput(int device_num) {
		if(device_num == -1) return false;

		MidiDevice.Info[] devices = availableOutputsMidiDeviceInfo();
		
		if(device_num >= devices.length || device_num < 0) {
			System.err.println("\nThe MidiBus Warning: The chosen output device numbered ["+device_num+"] was not added because it doesn't exist");
			return false;
		}
		
		return addInput(devices[device_num]);		
	}
	
	/**
	 * Removes the Midi output device specified by the index device_num.
	 *
	 * @param device_num the index of the Midi output device to be removed
	 * @return true if and only if the output device was successfully removed
	 * @see #removeInput(String device_name)
	 * @see #attachedOutputs()
	*/
	public boolean removeOutput(int device_num) {
		try {
			OutputDeviceContainer container = output_devices.get(device_num);
		
			container.receiver.close();
			container.device.close();
		
			output_devices.remove(container);
			return true;
		} catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}
	
	/**
	 * Adds a new Midi output device specified by the name device_name. If the Midi output device has already been added, it will not be added again.
	 * <p>
	 * If two or more Midi outputs have the same name, whichever appears first when {@link #list()} is called will be added. If this behavior is problematic use {@link #addOutput(int device_num)} instead.
	 *
	 * @param device_name the name of the Midi output device to be added
	 * @return true if and only if the output device was successfully added
	 * @see #addOutput(int device_num)
	 * @see #list()
	*/
	public boolean addOutput(String device_name) {
		if(device_name.equals("")) return false;
		
		MidiDevice.Info[] devices = availableOutputsMidiDeviceInfo();
		
		for(int i = 0;i < devices.length;i++) {
			if(devices[i].getName().equals(device_name)) return addOutput(devices[i]);
		}
		
		System.err.println("\nThe MidiBus Warning: No available input Midi devices named: \""+device_name+"\" were found");
		return false;	
	}
	
	/**
	 * Removes the Midi output device specified by the name device_name.
	 * <p>
	 * If two or more attached Midi outputs have the same name, whichever appears first when {@link #attachedOutputs()} is called will be removed. If this behavior is problematic use {@link #removeOutput(int device_num)} instead.
	 *
	 * @param device_name the name of the Midi output device to be removed
	 * @return true if and only if the output device was successfully removed
	 * @see #removeOutput(int device_num)
	 * @see #attachedOutputs()
	*/
	public boolean removeOutput(String device_name) {
		for(OutputDeviceContainer container : output_devices) {
			if(container.info.getName().equals(device_name)) {
				container.receiver.close();
				container.device.close();

				output_devices.remove(container);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds a new Midi output device specified by the MidiDevice.Info device_info. If the Midi output device has already been added, it will not be added again.
	 *
	 * @param device_info the MidiDevice.Info of the Midi output device to be added
	 * @return true if and only if the input device was successfully added
	*/
	boolean addOutput(MidiDevice.Info device_info) {
		try {
			MidiDevice new_device = MidiSystem.getMidiDevice(device_info);
		
			if(new_device.getMaxReceivers() == 0) {
				System.err.println("\nThe MidiBus Warning: The chosen output device \""+device_info.getName()+"\" was not added because it is input only");
				return false;
			}
			
			for(OutputDeviceContainer container : output_devices) {
				if(device_info.equals(container.info)) return false;
			}

			new_device.open();
			
			OutputDeviceContainer new_container = new OutputDeviceContainer(new_device);
			new_container.receiver = new_device.getReceiver();
						
			output_devices.add(new_container);
			
			return true;
		} catch (MidiUnavailableException e) {
			System.err.println("\nThe MidiBus Warning: The chosen output device \""+device_info.getName()+"\" was not added because it is unavailable");
			return false;
		}
	}
		
	/**
	 * Closes, clears and disposes of all input related MidiDevices, Transmitters and Receivers.
	 *
	 * @see #clearOutputs()
	 * @see #clearAll()
	*/
	public void clearInputs() {
		for(InputDeviceContainer container : input_devices) {
			container.transmitter.close();
			container.receiver.close();
			container.device.close();
		}
		
		input_devices.clear();
	}
	
	/**
	 * Closes, clears and disposes of all output related MidiDevices and Receivers.
	 *
	 * @see #clearInputs()
	 * @see #clearAll()
	*/
	public void clearOutputs() {
		for(OutputDeviceContainer container : output_devices) {
			container.receiver.close();
			container.device.close();
		}
		
		output_devices.clear();
	}
	
	/**
	 * Closes, clears and disposes of all input and output related MidiDevices, Transmitters and Receivers.
	 *
	 * @see #clearInputs()
	 * @see #clearOutputs()
	*/
	public void clearAll() {
		clearInputs();
		clearOutputs();
	}
	
	/* -- Midi Out -- */
	
	/**
	 * Sends a Midi message with an unspecified number of bytes. 
	 *
	 * @param data the bytes of the Midi message
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendControllerChange(int channel, int number, int value)
	*/
	public void sendMessage(byte[] data) {
		if((int)((byte)data[0] & 0xFF) == MetaMessage.META) {
				MetaMessage message = new MetaMessage();
				try {
					byte[] payload = new byte[data.length-2];
					System.arraycopy(data, 2, payload, 0, data.length-2);
					message.setMessage((int)((byte)data[1] & 0xFF), payload, data.length-2);
					sendMessage(data);
				} catch(InvalidMidiDataException e) {
					System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
				}
			} else if((int)((byte)data[0] & 0xFF) == SysexMessage.SYSTEM_EXCLUSIVE || (int)((byte)data[0] & 0xFF) == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
				SysexMessage message = new SysexMessage();
				try {
					message.setMessage(data, data.length);
					sendMessage(message);
				} catch(InvalidMidiDataException e) {
					System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
				}
			} else {
				ShortMessage message = new ShortMessage();
				try {
					message.setMessage((int)((byte)data[0] & 0xFF), (int)((byte)data[1] & 0xFF), (int)((byte)data[2] & 0xFF));
					sendMessage(message);
				} catch(InvalidMidiDataException e) {
					System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
				}
			}
	}
	
	/**
	 * Sends a Midi message that takes no data bytes.
	 *
	 * @param status the status byte
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendControllerChange(int channel, int number, int value)
	*/
	public void sendMessage(int status) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(status);
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
		}
	}
	
	/**
	 * Sends a Midi message that takes only one data byte. If the message does not take data, the data byte is ignored.
	 *
	 * @param status the status byte
	 * @param data the data byte
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendControllerChange(int channel, int number, int value)
	*/
	public void sendMessage(int status, int data) {
		sendMessage(status, data, 0);
	}
	
	/**
	 * Sends a Midi message that takes one or two data bytes. If the message takes only one data byte, the second data byte is ignored; if the message does not take any data bytes, both data bytes are ignored.
	 *
	 * @param status the status byte
	 * @param data1 the first data byte
	 * @param data2 the second data byte
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendControllerChange(int channel, int number, int value)
	*/
	public void sendMessage(int status, int data1, int data2) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(status, data1, data2);
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
		}
	}
	
	/**
	 * Sends a channel message which takes up to two data bytes. If the message only takes one data byte, the second data byte is ignored; if the message does not take any data bytes, both data bytes are ignored.
	 *
	 * @param command the Midi command represented by this message
	 * @param channel the channel associated with the message
	 * @param data1 the first data byte
	 * @param data2 the second data byte
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendControllerChange(int channel, int number, int value)
	*/
	public void sendMessage(int command, int channel, int data1, int data2) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(command, channel, data1, data2);
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
		}
	}
	
	/**
	 * Sends a MidiMessage object.
	 *
	 * @param message the MidiMessage
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendControllerChange(int channel, int number, int value)
	*/
	public void sendMessage(MidiMessage message) {
		for(OutputDeviceContainer container : output_devices) {
			container.receiver.send(message,-1);
		}
	}
	
	/**
	 * Sends a NoteOn message to a channel with the specified pitch and velocity.
	 *
	 * @param channel the channel associated with the message
	 * @param pitch the pitch associated with the message
	 * @param velocity the velocity associated with the message
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	 * @see #sendControllerChange(int channel, int number, int value)
	*/
	public void sendNoteOn(int channel, int pitch, int velocity) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(ShortMessage.NOTE_ON, constrain(channel,0,15), constrain(pitch,0,127), constrain(velocity,0,127));
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
		}
	}
	
	/**
	 * Sends a NoteOff message to a channel with the specified pitch and velocity.
	 *
	 * @param channel the channel associated with the message
	 * @param pitch the pitch associated with the message
	 * @param velocity the velocity associated with the message
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendControllerChange(int channel, int number, int value)
	*/
	public void sendNoteOff(int channel, int pitch, int velocity) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(ShortMessage.NOTE_OFF, constrain(channel,0,15), constrain(pitch,0,127), constrain(velocity,0,127));
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
		}
	}
	
	/**
	 * Sends a ControllerChange message to a channel with the specified number and value.
	 *
	 * @param channel the channel associated with the message
	 * @param number the number associated with the message
	 * @param value the value associated with the message
	 * @see #sendMessage(byte[] data)
	 * @see #sendMessage(int status)
	 * @see #sendMessage(int status, int data)
	 * @see #sendMessage(int status, int data1, int data2)
	 * @see #sendMessage(int command, int channel, int data1, int data2)
	 * @see #sendMessage(MidiMessage message)
	 * @see #sendNoteOn(int channel, int pitch, int velocity)
	 * @see #sendNoteOff(int channel, int pitch, int velocity)
	*/
	public void sendControllerChange(int channel, int number, int value) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(ShortMessage.CONTROL_CHANGE, constrain(channel,0,15), constrain(number,0,127), constrain(value,0,127));
			sendMessage(message);
		} catch(InvalidMidiDataException e) {
			System.err.println("\nThe MidiBus Warning: Message not sent, invalid Midi data");
		}
	}
	
	/* -- Midi In -- */
	
	/**
	 * Notifies all types of listeners of a new Midi message from one of the Midi input devices.
	 *
	 * @param message the new inbound MidiMessage
	*/
	void notifyListeners(MidiMessage message) {
		byte[] data = message.getMessage();
		
		for(MidiListener listener : listeners) {
		
			/* -- RawMidiListener -- */
		
			if(listener instanceof RawMidiListener) {
	 			((RawMidiListener)listener).rawMidiMessage(data);
			}
		
			/* -- SimpleMidiListener -- */
			
			if(listener instanceof SimpleMidiListener) {
				if((int)((byte)data[0] & 0xF0) == ShortMessage.NOTE_ON) {
					((SimpleMidiListener)listener).noteOn((int)(data[0] & 0x0F),(int)(data[1] & 0xFF),(int)(data[2] & 0xFF));
				} else if((int)((byte)data[0] & 0xF0) == ShortMessage.NOTE_OFF) {
					((SimpleMidiListener)listener).noteOff((int)(data[0] & 0x0F),(int)(data[1] & 0xFF),(int)(data[2] & 0xFF));
				} else if((int)((byte)data[0] & 0xF0) == ShortMessage.CONTROL_CHANGE) {
					((SimpleMidiListener)listener).controllerChange((int)(data[0] & 0x0F),(int)(data[1] & 0xFF),(int)(data[2] & 0xFF));
				}
			}
		
			/* -- StandardMidiListener -- */
		
			if(listener instanceof StandardMidiListener) {
				((StandardMidiListener)listener).midiMessage(message);
			}
			
		}
	}
	
	/**
	 * Notifies any of the supported methods implemented inside the PApplet parent of a new Midi message from one of the Midi input devices.
	 *
	 * @param message the new inbound MidiMessage
	*/
	void notifyPApplet(MidiMessage message) {	
		byte[] data = message.getMessage();

		if((int)((byte)data[0] & 0xF0) == ShortMessage.NOTE_ON) {
			if(eventMethod_noteOn != null) {
				try {
					eventMethod_noteOn.invoke(parent, new Object[] { (int)(data[0] & 0x0F), (int)(data[1] & 0xFF), (int)(data[2] & 0xFF) });
				} catch (Exception e) {
					System.err.println("\nThe MidiBus Warning: Disabling noteOn(int channel, int pitch, int velocity) because an unkown exception was thrown and caught");
					e.printStackTrace();
					eventMethod_noteOn = null;
				}
			}
			if(eventMethod_noteOn_withBusName != null) {
				try {
					eventMethod_noteOn_withBusName.invoke(parent, new Object[] { (int)(data[0] & 0x0F), (int)(data[1] & 0xFF), (int)(data[2] & 0xFF), bus_name });
				} catch (Exception e) {
					System.err.println("\nThe MidiBus Warning: Disabling noteOn(int channel, int pitch, int velocity, String bus_name) with bus_name because an unkown exception was thrown and caught");
					e.printStackTrace();
					eventMethod_noteOn_withBusName = null;
				}
			}
		} else if((int)((byte)data[0] & 0xF0) == ShortMessage.NOTE_OFF) {
			if(eventMethod_noteOff != null) {
				try {
					eventMethod_noteOff.invoke(parent, new Object[] { (int)(data[0] & 0x0F), (int)(data[1] & 0xFF), (int)(data[2] & 0xFF) });
				} catch (Exception e) {
					System.err.println("\nThe MidiBus Warning: Disabling noteOff(int channel, int pitch, int velocity) because an unkown exception was thrown and caught");
					e.printStackTrace();
					eventMethod_noteOff = null;
				}
			}
			if(eventMethod_noteOff_withBusName != null) {
				try {
					eventMethod_noteOff_withBusName.invoke(parent, new Object[] { (int)(data[0] & 0x0F), (int)(data[1] & 0xFF), (int)(data[2] & 0xFF), bus_name });
				} catch (Exception e) {
					System.err.println("\nThe MidiBus Warning: Disabling noteOff(int channel, int pitch, int velocity, String bus_name) with bus_name because an unkown exception was thrown and caught");
					e.printStackTrace();
					eventMethod_noteOff_withBusName = null;
				}
			}
		} else if((int)((byte)data[0] & 0xF0) == ShortMessage.CONTROL_CHANGE) {
			if(eventMethod_controllerChange != null) {
				try {
					eventMethod_controllerChange.invoke(parent, new Object[] { (int)(data[0] & 0x0F), (int)(data[1] & 0xFF), (int)(data[2] & 0xFF) });
				} catch (Exception e) {
					System.err.println("\nThe MidiBus Warning: Disabling controllerChange(int channel, int number, int value) because an unkown exception was thrown and caught");
					e.printStackTrace();
					eventMethod_controllerChange = null;
				}
			}
			if(eventMethod_controllerChange_withBusName != null) {
				try {
					eventMethod_controllerChange_withBusName.invoke(parent, new Object[] { (int)(data[0] & 0x0F), (int)(data[1] & 0xFF), (int)(data[2] & 0xFF), bus_name });
				} catch (Exception e) {
					System.err.println("\nThe MidiBus Warning: Disabling controllerChange(int channel, int number, int value, String bus_name) with bus_name because an unkown exception was thrown and caught");
					e.printStackTrace();
					eventMethod_controllerChange_withBusName = null;
				}
			}
		}
		
		if(eventMethod_rawMidi != null) {
			try {
				eventMethod_rawMidi.invoke(parent, new Object[] { data });
			} catch (Exception e) {
				System.err.println("\nThe MidiBus Warning: Disabling rawMidi(byte[] data) because an unkown exception was thrown and caught");
				e.printStackTrace();
				eventMethod_rawMidi = null;
			}
		}
		if(eventMethod_rawMidi_withBusName != null) {
			try {
				eventMethod_rawMidi_withBusName.invoke(parent, new Object[] { data, bus_name });
			} catch (Exception e) {
				System.err.println("\nThe MidiBus Warning: Disabling rawMidi(byte[] data, String bus_name) with bus_name because an unkown exception was thrown and caught");
				e.printStackTrace();
				eventMethod_rawMidi_withBusName = null;
			}
		}
		
		if(eventMethod_midiMessage != null) {
			try {
				eventMethod_midiMessage.invoke(parent, new Object[] { message });
			} catch (Exception e) {
				System.err.println("\nThe MidiBus Warning: Disabling midiMessage(MidiMessage message) because an unkown exception was thrown and caught");
				e.printStackTrace();
				eventMethod_midiMessage = null;
			}
		}
		if(eventMethod_midiMessage_withBusName != null) {
			try {
				eventMethod_midiMessage_withBusName.invoke(parent, new Object[] { message, bus_name });
			} catch (Exception e) {
				System.err.println("\nThe MidiBus Warning: Disabling midiMessage(MidiMessage message, String bus_name) with bus_name because an unkown exception was thrown and caught");
				e.printStackTrace();
				eventMethod_midiMessage_withBusName = null;
			}
		}
		
	}
	
	/* -- Listener Handling -- */
	
	/**
	 * 	Adds a listener who will be notified each time a new Midi message is received from a Midi input device. If the listener has already been added, it will not be added again.
	 *
	 * @param listener the listener to add
	 * @return true if and only the listener was sucessfully added
	 * @see #removeMidiListener(MidiListener listener)
	*/
	public boolean addMidiListener(MidiListener listener) {
		for(MidiListener current : listeners) if(current == listener) return false;
		
		listeners.add(listener);
				
		return true;
	}
	
	/**
	 * Removes a given listener.
	 *
	 * @param listener the listener to remove
	 * @return true if and only the listener was sucessfully removed
	 * @see #addMidiListener(MidiListener listener)
	*/
	public boolean removeMidiListener(MidiListener listener) {
		for(MidiListener current : listeners) {
			if(current == listener) {
				listeners.remove(listener);
				return true;
			}
		}
		return false;
	}
	
	
	/* -- Utilites -- */
	
	/**
	 * It's just convient ... move along...
	*/
	int constrain(int value, int min, int max) {
		if(value > max) value = max;
		if(value < min) value = min;
		return value;
	}
	
	/**
	 * Returns the name of this MidiBus.
	 *
	 * @return the name of this MidiBus
	 * @see #setBusName(String bus_name)
	*/
	public String getBusName() {
		return bus_name;
	}
	
	/**
	 * Changes the name of this MidiBus.
	 *
	 * @param bus_name the new name of this MidiBus
	 * @see #getBusName()
	*/
	public void setBusName(String bus_name) {
		this.bus_name = bus_name;
	}
	
	/* -- Object -- */
	
	/**
	 * Returns a string representation of the object
	 *
	 * @return a string representation of the object.
	 */
	public String toString() {
		String output = "MidiBus: "+bus_name+" [";
		output += input_devices.size()+" input(s), ";
		output += output_devices.size()+" output(s), ";
		output += listeners.size()+" listener(s)]";
		return output;
	}
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 *
	 * @param obj the reference object with which to compare.
	 * @return if this object is the same as the obj argument; false otherwise.
	 */
	public boolean equals(Object obj) {
		if(obj instanceof MidiBus) {
			MidiBus midibus = (MidiBus)obj;
			if(!this.getBusName().equals(midibus.getBusName())) return false;
			if(!this.input_devices.equals(midibus.input_devices)) return false;
			if(!this.output_devices.equals(midibus.output_devices)) return false;
			if(!this.listeners.equals(midibus.listeners)) return false;
			return true;
		}
		return false;
	}
	
	/**
	 * Creates and returns a copy of this object.
	 *
	 * @return a clone of this instance.
	 */
	public MidiBus clone() {
		MidiBus clone = new MidiBus(parent, bus_name);
		
		for(InputDeviceContainer container : input_devices) {
			clone.addInput(container.info);
		}
		
		for(OutputDeviceContainer container : output_devices) {
			clone.addOutput(container.info);
		}
		
		for(MidiListener listener : listeners) {
			clone.addMidiListener(listener);
		}
		
		return clone;
	}
	
	/**
	 * Returns a hash code value for the object.
	 *
	 * @return a hash code value for this object.
	 */
	public int hashCode() {
		return bus_name.hashCode()+input_devices.hashCode()+output_devices.hashCode()+listeners.hashCode();
	}
	
	/**
	 * Override the finalize() method from java.lang.Object
	 *
	*/
	protected void finalize() {
		close();
		parent.unregisterDispose(this);
	}
	
	/* -- Shutting Down -- */
	
	/**
	 * Closes this MidiBus and all connections it has with other Midi devices. This method exists as per standard javax.sound.midi syntax. It is functionaly equivalent to stop() and dispose().
	 *
	 * @see #stop()
	 * @see #dispose()
	*/
	public void close() {		
		clearAll();
	}
		
	/**
	 * Closes this MidiBus and all connections it has with other Midi devices. This method exit as per standard Processing syntax for users who are doing their sketch cleanup themselves using the stop() function. It is functionaly equivalent to close() and dispose().
	 *
	 * @see #close()
	 * @see #dispose()
	*/
	public void stop() {
		close();
	}
	
	/**
	 * Closes this MidiBus and all connections it has with other Midi devices. This method exit as per standard Processing library syntax and is called automatically whenever the parent applet shuts down. It is functionaly equivalent to close() and stop().
	 *
	 * @see #close()
	 * @see #stop()
	*/
	public void dispose() {
		close();
	}
	
	/* -- Static methods -- */
	
	/**
	 * Lists the name and index of all MidiDevices available and indicates if they are inputs, outputs or both.
	 *
	*/
	static public void list() {
		String[] available_inputs = availableInputs();
		String[] available_outputs = availableOutputs();
		String[] unavailable = unavailableDevices();
		
		if(available_inputs.length == 0 && available_outputs.length == 0 && unavailable.length == 0) return;
		
		System.out.println("\nAvailable Midi Devices:");
		if(available_inputs.length != 0) {
			System.out.println("----------Input----------");
			for(int i = 0;i < available_inputs.length;i++) System.out.println("["+i+"] \""+available_inputs[i]+"\"");
		}
		if(available_outputs.length != 0) {
			System.out.println("----------Output----------");
			for(int i = 0;i < available_outputs.length;i++) System.out.println("["+i+"] \""+available_outputs[i]+"\"");
		}
		if(unavailable.length != 0) {
			System.out.println("----------Unavailable----------");
			for(int i = 0;i < unavailable.length;i++) System.out.println("["+i+"] \""+unavailable[i]+"\"");
		}
	}
	
	static public String[] availableInputs() {
		MidiDevice.Info[] devices_info = availableInputsMidiDeviceInfo();
		String[] devices = new String[devices_info.length];
		
		for(int i = 0;i < devices_info.length;i++) {
			devices[i] = devices_info[i].getName();
		}
		
		return devices;
	}
	
	static public String[] availableOutputs() {
		MidiDevice.Info[] devices_info = availableOutputsMidiDeviceInfo();
		String[] devices = new String[devices_info.length];
		
		for(int i = 0;i < devices_info.length;i++) {
			devices[i] = devices_info[i].getName();
		}
		
		return devices;
	}
	
	static public String[] unavailableDevices() {
		MidiDevice.Info[] devices_info = unavailableMidiDeviceInfo();
		String[] devices = new String[devices_info.length];
		
		for(int i = 0;i < devices_info.length;i++) {
			devices[i] = devices_info[i].getName();
		}
		
		return devices;
	}
	
	static MidiDevice.Info[] availableInputsMidiDeviceInfo() {
		MidiDevice.Info[] available_devices = MidiSystem.getMidiDeviceInfo();
		MidiDevice device;
		
		Vector<MidiDevice.Info> devices_list = new Vector<MidiDevice.Info>();
		
		for(int i = 0;i < available_devices.length;i++) {
			try {
				device = MidiSystem.getMidiDevice(available_devices[i]);
				device.open();
				device.close();
				if (device.getMaxTransmitters() != 0) devices_list.add(available_devices[i]);
			} catch (MidiUnavailableException e) {

			}
		}
		
		MidiDevice.Info[] devices = new MidiDevice.Info[devices_list.size()];
		
		devices_list.toArray(devices);
		
		return devices;
	}
	
	static MidiDevice.Info[] availableOutputsMidiDeviceInfo() {
		MidiDevice.Info[] available_devices = MidiSystem.getMidiDeviceInfo();
		MidiDevice device;
		
		Vector<MidiDevice.Info> devices_list = new Vector<MidiDevice.Info>();
		
		for(int i = 0;i < available_devices.length;i++) {
			try {
				device = MidiSystem.getMidiDevice(available_devices[i]);
				device.open();
				device.close();
				if (device.getMaxReceivers() != 0) devices_list.add(available_devices[i]);
			} catch (MidiUnavailableException e) {

			}
		}
		
		MidiDevice.Info[] devices = new MidiDevice.Info[devices_list.size()];
		
		devices_list.toArray(devices);
		
		return devices;
	}
	
	static MidiDevice.Info[] unavailableMidiDeviceInfo() {
		MidiDevice.Info[] available_devices = MidiSystem.getMidiDeviceInfo();
		MidiDevice device;
		
		Vector<MidiDevice.Info> devices_list = new Vector<MidiDevice.Info>();
		
		for(int i = 0;i < available_devices.length;i++) {
			try {
				device = MidiSystem.getMidiDevice(available_devices[i]);
				device.open();
				device.close();
			} catch (MidiUnavailableException e) {
				devices_list.add(available_devices[i]);
			}
		}
		
		MidiDevice.Info[] devices = new MidiDevice.Info[devices_list.size()];
		
		devices_list.toArray(devices);
		
		return devices;
	}
		
	/* -- Nested Classes -- */
	
	private class MReceiver implements Receiver {
				
		MReceiver() {

		}
		
		public void close() {

		}
		
	 	public void send(MidiMessage message, long timeStamp) {
			
			if(message.getStatus() == ShortMessage.NOTE_ON && message.getMessage()[2] == 0) {
				try {
					ShortMessage tmp_message = (ShortMessage)message;
					tmp_message.setMessage(ShortMessage.NOTE_OFF, tmp_message.getData1(), tmp_message.getData2());
					message = tmp_message;
				} catch (Exception e) {
					System.err.println("\nThe MidiBus Warning: Mystery error during noteOn (0 velocity) to noteOff conversion");
				}
			}
			
			notifyListeners(message);
			notifyPApplet(message);
		}
		
	}	
	
	private class InputDeviceContainer {
		
		MidiDevice device;
		
		MidiDevice.Info info;
		
		Transmitter transmitter;
		Receiver receiver;
		
		InputDeviceContainer(MidiDevice device) {
			this.device = device;
			this.info = device.getDeviceInfo();
		}
		
		public boolean equals(Object container) {
			if(container instanceof InputDeviceContainer && ((InputDeviceContainer)container).info.equals(this.info)) return true;
			else return false;
		}
		
	}
	
	private class OutputDeviceContainer {
		
		MidiDevice device;
	
		MidiDevice.Info info;
		
		Receiver receiver;
		
		OutputDeviceContainer(MidiDevice device) {
			this.device = device;
			this.info = device.getDeviceInfo();
		}
		
		public boolean equals(Object container) {
			if(container instanceof OutputDeviceContainer && ((OutputDeviceContainer)container).info.equals(this.info)) return true;
			else return false;
		}
	}
	
}