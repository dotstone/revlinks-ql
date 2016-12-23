package at.jku.isse.cloud.bank;

import at.jku.isse.cloud.artifact.DSClass;
import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.isse.cloud.artifact.DSLink;


public class BankExample {	
	
    @SuppressWarnings("unused")
	public static void main(String[] args ) {
    	DSConnection conn = new DSConnection("dos", "mepwd", "my workspace", "some other package");
    	
    	DSClass transaction = new DSClass(conn, "Transaction").withFeatures("amount");
    	DSClass headOffice = new DSClass(conn, "HeadOffice").withFeatures("bankName", "address");
    	DSClass customer = new DSClass(conn, "Customer").withFeatures("name", "address");
    	DSClass branch = new DSClass(conn, "Branch")
    			.withFeatures("address", "manager")
    			.withLinks(new DSLink("location", headOffice, 1, -1, 1, 1));
    	DSClass account = new DSClass(conn, "BankAccount")
    			.withFeatures("accountNo", "balance")
    			.withOperations("returnBalance", "updateBalance")
    			.withLinks(new DSLink("executed transactions", transaction, 0, -1, 1, 1), new DSLink("branches", branch, 1, 1, 1, -1),
    					new DSLink("belongs to", customer, 1, -1, 1, 1));
    	DSClass currentAccount = new DSClass(conn, "CurrentAccount")
				.withOperations("calculateCharges", "calculateInterest")
				.withSuperType(account);
    	DSClass savingsAccount = new DSClass(conn, "SavingsAccount")
				.withOperations("calculateCharges", "calculateInterest")
				.withSuperType(account);
        
        conn.commit("create bank classes");
        System.out.println("Finished");
    }
}