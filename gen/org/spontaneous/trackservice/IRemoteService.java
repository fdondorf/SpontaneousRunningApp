/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Projekte\\SpontaneousRunning\\workspaceApp\\main\\SpontaneousRunningApp\\src\\org\\spontaneous\\trackservice\\IRemoteService.aidl
 */
package org.spontaneous.trackservice;
/**
* Example of defining an interface for calling on to a remote service
* (running in another process).
*/
public interface IRemoteService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.spontaneous.trackservice.IRemoteService
{
private static final java.lang.String DESCRIPTOR = "org.spontaneous.trackservice.IRemoteService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.spontaneous.trackservice.IRemoteService interface,
 * generating a proxy if needed.
 */
public static org.spontaneous.trackservice.IRemoteService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.spontaneous.trackservice.IRemoteService))) {
return ((org.spontaneous.trackservice.IRemoteService)iin);
}
return new org.spontaneous.trackservice.IRemoteService.Stub.Proxy(obj);
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
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
org.spontaneous.trackservice.IRemoteServiceCallback _arg0;
_arg0 = org.spontaneous.trackservice.IRemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
org.spontaneous.trackservice.IRemoteServiceCallback _arg0;
_arg0 = org.spontaneous.trackservice.IRemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_loggingState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.loggingState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_startLogging:
{
data.enforceInterface(DESCRIPTOR);
android.location.Location _arg0;
if ((0!=data.readInt())) {
_arg0 = android.location.Location.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
long _result = this.startLogging(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_pauseLogging:
{
data.enforceInterface(DESCRIPTOR);
this.pauseLogging();
reply.writeNoException();
return true;
}
case TRANSACTION_resumeLogging:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
long _result = this.resumeLogging(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_stopLogging:
{
data.enforceInterface(DESCRIPTOR);
this.stopLogging();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.spontaneous.trackservice.IRemoteService
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
	* Often you want to allow a service to call back to its clients.
	* This shows how to do so, by registering a callback interface with
	* the service.
	*/
@Override public void registerCallback(org.spontaneous.trackservice.IRemoteServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	* Remove a previously registered callback interface.
	*/
@Override public void unregisterCallback(org.spontaneous.trackservice.IRemoteServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int loggingState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_loggingState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public long startLogging(android.location.Location startLocation) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((startLocation!=null)) {
_data.writeInt(1);
startLocation.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_startLogging, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void pauseLogging() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pauseLogging, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public long resumeLogging(long trackId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(trackId);
mRemote.transact(Stub.TRANSACTION_resumeLogging, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void stopLogging() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopLogging, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_loggingState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_startLogging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_pauseLogging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_resumeLogging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_stopLogging = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
/**
	* Often you want to allow a service to call back to its clients.
	* This shows how to do so, by registering a callback interface with
	* the service.
	*/
public void registerCallback(org.spontaneous.trackservice.IRemoteServiceCallback cb) throws android.os.RemoteException;
/**
	* Remove a previously registered callback interface.
	*/
public void unregisterCallback(org.spontaneous.trackservice.IRemoteServiceCallback cb) throws android.os.RemoteException;
public int loggingState() throws android.os.RemoteException;
public long startLogging(android.location.Location startLocation) throws android.os.RemoteException;
public void pauseLogging() throws android.os.RemoteException;
public long resumeLogging(long trackId) throws android.os.RemoteException;
public void stopLogging() throws android.os.RemoteException;
}
