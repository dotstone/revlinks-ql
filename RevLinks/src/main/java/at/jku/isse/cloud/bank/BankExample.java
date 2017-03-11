package at.jku.isse.cloud.bank;

import at.jku.isse.cloud.artifact.DSClass;

import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.isse.cloud.artifact.DSLink;
import at.jku.sea.cloud.Package;

public class BankExample {	
	
    @SuppressWarnings("unused")
	public static void main(String[] args ) {
    	DSConnection conn = new DSConnection("Bank_user", "Bank_pwd", "bank_workspace");
    	Package pkg = conn.getOrCreatePackage("Bank");
    	
    	DSClass transaction = new DSClass(conn, "Transaction", pkg).withFeatures("amount");
    	DSClass headOffice = new DSClass(conn, "HeadOffice", pkg).withFeatures("bankName", "address");
    	DSClass customer = new DSClass(conn, "Customer", pkg).withFeatures("name", "address");
    	DSClass branch = new DSClass(conn, "Branch", pkg)
    			.withFeatures("address", "manager")
    			.withLinks(new DSLink("location", headOffice, 1, -1, 1, 1));
    	DSClass account = new DSClass(conn, "BankAccount", pkg)
    			.withFeatures("accountNo", "balance")
    			.withOperations("returnBalance", "updateBalance")
    			.withLinks(new DSLink("executed transactions", transaction, 0, -1, 1, 1), new DSLink("branches", branch, 1, 1, 1, -1),
    					new DSLink("belongs to", customer, 1, -1, 1, 1));
    	DSClass currentAccount = new DSClass(conn, "CurrentAccount", pkg)
				.withOperations("calculateCharges", "calculateInterest")
				.withSuperType(account);
    	DSClass savingsAccount = new DSClass(conn, "SavingsAccount", pkg)
				.withOperations("calculateCharges", "calculateInterest")
				.withSuperType(account);
        
        conn.commit("create bank classes");
        System.out.println("Finished");
    }
}