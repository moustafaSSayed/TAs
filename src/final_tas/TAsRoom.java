package final_tas;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class TAsRoom {
    
    private static Semaphore TA;//semaphore for the Teaching Assistants
    private static Semaphore Student;//semaphore for the Students
    private static Semaphore chairs;//semaphore to access the waiting chairs
    private static ReentrantLock lock;
    private static int waitingChairs ;//number of chairs
    private static int studentssNumber ;//number of student
    private static int TAsNumber;//number of TAs
    private static int HelpedStudent=0;
    private static int SleepingTAs=0;
    static AtomicBoolean [] back;
    public TAsRoom(int WaitingChairs,int StudentsNumber,int TAsNumber){
        
        this.waitingChairs=WaitingChairs;
        this.studentssNumber=StudentsNumber;
        this.TAsNumber=TAsNumber;
        TA = new Semaphore(TAsNumber);
        Student = new Semaphore(0);
        chairs = new Semaphore(WaitingChairs);
        back=new AtomicBoolean[studentssNumber];
        lock=new ReentrantLock();
        for (int i = 0; i < studentssNumber; i++) {
            back[i] = new AtomicBoolean(false);
        }
    }
    
    static void TA(int i) {
          new Thread(() -> {
              try {
                  while(true) {
                      System.out.println("TA "+i+" sleeping");
                      SleepingTAs++;
                      Student.acquire();
                      SleepingTAs--;
                      System.out.println("TA "+i+"  got Student");
                      TA.release();
                      waitingChairs++;
                      System.out.println("TA "+i+" is helping student");
                      Thread.sleep(1000);
                      System.out.println("TA "+i+" helping is done");
                  }
            }
            catch(InterruptedException e) {
                System.out.println("the TA "+i+" work is interrypted somehow");
            }
        }).start();
    }

    static void Student(int i) throws InterruptedException {
        if(getHelpedStudent()<studentssNumber){
            Thread thStudent = new Thread(new Runnable() {
               @Override
               public void run() {
                try {
                   AtomicBoolean[] temp = new AtomicBoolean[studentssNumber];
                   temp = back;
                   System.out.println("Student "+(i+1)+": checking seats");
                   chairs.acquire();
                   if(waitingChairs==0) {
                       temp[i].set(true);
                       System.out.println("Student "+(i+1)+": no seats! i will come back later");
                       chairs.release();
                       back=temp;
                   }
                   else
                   {
                       waitingChairs--;
                       Student.release();
                       chairs.release();
                       System.out.println("Student "+(i+1)+": found a seat ,number of empty seats="+ waitingChairs);
                       TA.acquire();
                       System.out.println("Student "+(i+1)+": is being helped");
                       temp[i].set(false);
                       back = temp;
                       lock.lock();
                       try {
                           HelpedStudent++;
                       } finally {
                           lock.unlock();
                       }
                   }
               }
               catch(InterruptedException e) {
                   System.out.println("Somthing happend when Student "+(i+1)+" searched for a chair");
               }

           }    
           });
           thStudent.start();
        }
    }
    
    static int getHelpedStudent(){
        int number;
        lock.lock();
        try {
            number =HelpedStudent;
        } finally {
            lock.unlock();
        }
        return number;
    }
    
    static int getSleepingTAs(){
        return SleepingTAs;
    }
    
}
