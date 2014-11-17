package Cherkasov.Artem.EntryPoint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FinderMain {
	private BlockingQueue<byte[]> blockingQueue = new ArrayBlockingQueue<byte[]>(10);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FinderMain finderMain = new FinderMain();
		finderMain.startFind();

	}
	
	public void startFind(){
		Object syncConsumer = new Object();
		Object syncProducer = new Object();		
		Producer producer = new Producer("out.txt", syncConsumer, syncProducer);
		Consumer consumer = new Consumer(syncConsumer, syncProducer);
		producer.start();
		consumer.start();
	}
	
	class Producer extends Thread {
		private Object syncConsumer;
		private Object syncProducer;
		private String fileName;
		
		public Producer(String fileName, Object syncConsumer, Object syncProducer) {
			// TODO Auto-generated constructor stub
			this.fileName = fileName;
			this.syncConsumer = syncConsumer;
			this.syncProducer = syncProducer;
		}
		
		@Override
		public void run() {

			FileInputStream fileInputStream;
			byte[] buffer = new byte[64];
			try {
				fileInputStream = new FileInputStream(fileName);
				int size_of_read = 0;
				
				synchronized (syncProducer) {
					while ((size_of_read = fileInputStream.read(buffer)) != -1) {
						try {
							blockingQueue.add(buffer);
						} catch (IllegalStateException e) {
							System.out.println("blockingQueue.size() = " + blockingQueue.size());
							synchronized (syncConsumer) {
								syncConsumer.notify();
							}
							syncProducer.wait();
						}
						buffer = new byte[64];
					}
					
				}

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		
	}
	
	class Consumer extends Thread{
		private Object syncConsumer;
		private Object syncProducer;
		public Consumer(Object syncConsumer, Object syncProducer) {
			// TODO Auto-generated constructor stub
			this.syncConsumer = syncConsumer;
			this.syncProducer = syncProducer;
		}
		
		@Override
		public void run() {
			
			try {
				
				synchronized (syncConsumer) {
					
					if (blockingQueue.isEmpty()){
						syncConsumer.wait();
						
					} else {

						while (!blockingQueue.isEmpty()) {
							System.out.println(new String(blockingQueue.take()));
							if(blockingQueue.isEmpty()){
								System.out.println("Buffer is empty!!!!!!!!!!!!!!!!!!!");
								synchronized (syncProducer){
									syncProducer.notify();
								}
								syncConsumer.wait();
							}
						}
					}

				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
