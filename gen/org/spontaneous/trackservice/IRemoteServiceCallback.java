/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Projekte\\SpontaneousRunning\\workspaceApp\\main\\SpontaneousRunningApp\\src\\org\\spontaneous\\trackservice\\IRemoteServiceCallback.aidl
 */
package org.spontaneous.trackservice;
/**
* Example of a callback interface used by IRemoteService to send
* synchronous notifications back to its clients. Note that this is a
* one-way interface so the server does not block waiting for the client.
*/
public interface IRemoteServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.spontaneous.trackservice.IRemoteServiceCallback
{
private static final java.lang.String DESCRIPTOR = "org.spontaneous.trackservice.IRemoteServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.spontaneous.trackservice.IRemoteServiceCallback interface,
 * generating a proxy if needed.
 */
public static org.spontaneous.trackservice.IRemoteServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.spontaneous.trackservice.IRemoteServiceCallback))) {
return ((org.spontaneous.trackservice.IRemoteServiceCallback)iin);
}
return new org.spontaneous.trackservice.IRemoteServiceCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_valueChanged:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.valueChanged(_arg0);
return true;
}
case TRANSACTION_locationChanged:
{
data.enforceInterface(DESCRIPTOR);
org.spontaneous.trackservice.WayPointModel _arg0;
if ((0!=data.readInt())) {
_arg0 = org.spontaneous.trackservice.WayPointModel.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.locationChanged(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.spontaneous.trackservice.IRemoteServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
	* Called when the service has a new value for you.
	*/
@Override public void valueChanged(int value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(value);
mRemote.transact(Stub.TRANSACTION_valueChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
@Override public void locationChanged(org.spontaneous.trackservice.WayPointModel wayPoint) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((wayPoint!=null)) {
_data.writeInt(1);
wayPoint.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_locationChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_valueChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_locationChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
	* Called when the service has a new value for you.
	*/
public void valueChanged(int value) throws android.os.RemoteException;
public void locationChanged(org.spontaneous.trackservice.WayPointModel wayPoint) throws android.os.RemoteException;
}
