import { useState } from "react";
import DashboardLayout from "@/components/DashboardLayout";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "@/components/ui/alert-dialog";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { toast } from "sonner";
import { Search, UserPlus, Loader2, Eye, Edit, Trash2 } from "lucide-react";
import { customerService, Customer } from "@/services/customerService";
import { CustomerViewModal } from "@/components/CustomerViewModal";
import { CustomerEditModal } from "@/components/CustomerEditModal";

const Customers = () => {
  const [aadharSearch, setAadharSearch] = useState("");
  const [searchedCustomer, setSearchedCustomer] = useState<Customer | null>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [searchNotFound, setSearchNotFound] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [deletingCustomer, setDeletingCustomer] = useState<Customer | null>(null);
  const [viewingCustomer, setViewingCustomer] = useState<Customer | null>(null);
  const [formData, setFormData] = useState({
    name: "",
    phoneNumber: "",
    email: "",
    address: "",
    customerPin: "",
    aadharNumber: "",
    dob: "",
    status: "Active"
  });

  const handleSearch = async () => {
    if (!aadharSearch.trim()) {
      toast.error("Please enter an Aadhaar number");
      return;
    }

    // Validate Aadhar format (12 digits)
    if (!aadharSearch.trim().match(/^\d{12}$/)) {
      toast.error("Aadhaar number must be exactly 12 digits");
      return;
    }
    
    setIsSearching(true);
    setSearchNotFound(false);
    setSearchedCustomer(null);
    
    try {
      const customer = await customerService.getCustomerByAadhar(aadharSearch.trim());
      if (customer) {
        setSearchedCustomer(customer);
        toast.success("Customer found!");
      }
    } catch (error: any) {
      if (error.response?.status === 404) {
        setSearchNotFound(true);
        toast.error("Customer not found");
      } else {
        toast.error("Failed to search customer");
        console.error(error);
      }
    } finally {
      setIsSearching(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    
    try {
      console.log('📤 Submitting customer creation...');
      
      // Frontend validation
      if (!formData.name.trim()) {
        toast.error("Name is required");
        setIsLoading(false);
        return;
      }
      
      if (!formData.phoneNumber.match(/^[1-9][0-9]{9}$/)) {
        toast.error("Phone number must be 10 digits and cannot start with 0");
        setIsLoading(false);
        return;
      }
      
      if (!formData.email.trim().match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/)) {
        toast.error("Invalid email format");
        setIsLoading(false);
        return;
      }
      
      if (!formData.customerPin.match(/^\d{6}$/)) {
        toast.error("Customer PIN must be exactly 6 digits");
        setIsLoading(false);
        return;
      }
      
      if (!formData.aadharNumber.match(/^\d{12}$/)) {
        toast.error("Aadhar number must be exactly 12 digits");
        setIsLoading(false);
        return;
      }
      
      if (!formData.dob) {
        toast.error("Date of birth is required");
        setIsLoading(false);
        return;
      }

      // Prepare data for backend
      const customerData = {
        name: formData.name.trim(),
        phoneNumber: formData.phoneNumber.trim(),
        email: formData.email.trim(),
        address: formData.address.trim(),
        customerPin: formData.customerPin.trim(),
        aadharNumber: formData.aadharNumber.trim(),
        dob: formData.dob,
        status: formData.status
      };

      const customerId = await customerService.createCustomer(customerData);
      
      console.log('✅ Customer created successfully! ID:', customerId);
      toast.success(`Customer created successfully!`);
      
      handleReset();
      setSearchedCustomer(null);
      setAadharSearch("");
      setSearchNotFound(false);
    } catch (error: any) {
      console.error('❌ Customer creation failed:', error);
      
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else if (error.response?.status === 400) {
        toast.error('Validation failed. Please check all required fields.');
      } else if (error.code === 'ERR_NETWORK') {
        toast.error('Cannot connect to server. Ensure backend is running on http://localhost:8080');
      } else {
        toast.error('Failed to create customer. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

const handleEdit = async (customer: Customer) => {
  if (!customer.aadharNumber || customer.aadharNumber.length !== 12) {
    toast.error("Aadhar number is missing or invalid. Cannot update customer.");
    return;
  }
  
  if (!customer.customerPin || customer.customerPin.length !== 6) {
    toast.error("Please enter your 6-digit PIN to confirm changes");
    return;
  }
  
  setIsLoading(true);
  
  try {
    console.log(' Updating customer via Aadhar:', customer.aadharNumber);
    
    await customerService.updateCustomerByAadhar(customer.aadharNumber, customer);
    
    toast.success("Customer updated successfully!");
    setEditingCustomer(null);
   
    if (searchedCustomer?.aadharNumber === customer.aadharNumber) {
      try {
        const updatedCustomer = await customerService.getCustomerByAadhar(customer.aadharNumber);
        setSearchedCustomer(updatedCustomer);
      } catch (error) {
        console.warn('Could not refresh customer data');
      }
    }
  } catch (error: any) {
    console.error(' Update failed:', error);
    
    if (error.response?.data?.message) {
      toast.error(error.response.data.message);
    } else if (error.response?.status === 400) {
      toast.error('Validation failed. Check all fields and try again.');
    } else if (error.response?.status === 404) {
      toast.error('Customer not found with this Aadhar number');
    } else if (error.message.includes('Customer ID is missing')) {
      toast.error('Unable to identify customer. Please try searching again.');
    } else {
      toast.error('Failed to update customer');
    }
  } finally {
    setIsLoading(false);
  }
};

  const handleDelete = async () => {
    if (!deletingCustomer) return;
    setIsLoading(true);
    try {
      await customerService.deleteCustomer(deletingCustomer.aadharNumber);
      toast.success("Customer deleted successfully!");
      setDeletingCustomer(null);
    
      if (searchedCustomer?.aadharNumber === deletingCustomer.aadharNumber) {
        setSearchedCustomer(null);
        setAadharSearch("");
      }
    } catch (error: any) {
      if (error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error("Failed to delete customer");
      }
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    setFormData({
      name: "",
      phoneNumber: "",
      email: "",
      address: "",
      customerPin: "",
      aadharNumber: "",
      dob: "",
      status: "Active"
    });
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold">Customer Management</h1>
          <p className="text-muted-foreground mt-1">Create and manage customer accounts</p>
        </div>

        {/* Create Customer Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <UserPlus className="h-5 w-5" />
              Create New Customer
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Full Name <span className="text-red-500">*</span></Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="Enter full name"
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="phoneNumber">Phone Number <span className="text-red-500">*</span></Label>
                  <Input
                    id="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      if (value.length <= 10) {
                        setFormData({ ...formData, phoneNumber: value });
                      }
                    }}
                    placeholder="10-digit mobile number"
                    required
                    maxLength={10}
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="email">Email <span className="text-red-500">*</span></Label>
                  <Input
                    id="email"
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    placeholder="email@example.com"
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="address">Address <span className="text-red-500">*</span></Label>
                  <Input
                    id="address"
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    placeholder="Enter address"
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="customerPin">Customer PIN <span className="text-red-500">*</span></Label>
                  <Input
                    id="customerPin"
                    type="password"
                    value={formData.customerPin}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      if (value.length <= 6) {
                        setFormData({ ...formData, customerPin: value });
                      }
                    }}
                    placeholder="6-digit PIN"
                    required
                    maxLength={6}
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="aadharNumber">Aadhar Number <span className="text-red-500">*</span></Label>
                  <Input
                    id="aadharNumber"
                    value={formData.aadharNumber}
                    onChange={(e) => {
                      const value = e.target.value.replace(/\D/g, '');
                      if (value.length <= 12) {
                        setFormData({ ...formData, aadharNumber: value });
                      }
                    }}
                    placeholder="12-digit Aadhar number"
                    required
                    maxLength={12}
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="dob">Date of Birth <span className="text-red-500">*</span></Label>
                  <Input
                    id="dob"
                    type="date"
                    value={formData.dob}
                    onChange={(e) => setFormData({ ...formData, dob: e.target.value })}
                    max={new Date(new Date().setFullYear(new Date().getFullYear() - 18)).toISOString().split('T')[0]}
                    required
                  />
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="status">Status</Label>
                  <Select value={formData.status} onValueChange={(value) => setFormData({ ...formData, status: value })}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {/* <SelectItem value="Inactive">Inactive</SelectItem> */}
                      <SelectItem value="Active">Active</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
              
              <div className="flex gap-3">
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin mr-2" />
                      Creating...
                    </>
                  ) : (
                    "Create Customer"
                  )}
                </Button>
                <Button type="button" variant="outline" onClick={handleReset}>
                  Reset
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Search Customer */}
        <Card>
          <CardHeader>
            <CardTitle>Search Customer</CardTitle>
            <p className="text-sm text-muted-foreground">Enter Aadhaar number to find customer details</p>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-3">
              <div className="flex-1 relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Enter 12-digit Aadhaar Number"
                  value={aadharSearch}
                  onChange={(e) => {
                    const value = e.target.value.replace(/\D/g, '');
                    if (value.length <= 12) {
                      setAadharSearch(value);
                    }
                  }}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  className="pl-9"
                  maxLength={12}
                />
              </div>
              <Button onClick={handleSearch} disabled={isSearching}>
                {isSearching ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin mr-2" />
                    Searching...
                  </>
                ) : (
                  <>
                    <Search className="h-4 w-4 mr-2" />
                    Search
                  </>
                )}
              </Button>
            </div>

            {searchNotFound && (
              <Alert>
                <AlertDescription>
                  No customer found with this Aadhaar number.
                </AlertDescription>
              </Alert>
            )}

            {searchedCustomer && (
              <Card className="border-2">
                <CardHeader>
                  <CardTitle className="text-lg">Customer Details</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    {searchedCustomer.customerId && (
                      <div>
                        <Label className="text-muted-foreground">Customer ID</Label>
                        <p className="font-medium">{searchedCustomer.customerId}</p>
                      </div>
                    )}
                    <div>
                      <Label className="text-muted-foreground">Name</Label>
                      <p className="font-medium">{searchedCustomer.name}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Email</Label>
                      <p className="font-medium">{searchedCustomer.email}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Phone</Label>
                      <p className="font-medium">{searchedCustomer.phoneNumber}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Aadhaar</Label>
                      <p className="font-medium">{searchedCustomer.aadharNumber}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">DOB</Label>
                      <p className="font-medium">{new Date(searchedCustomer.dob).toLocaleDateString()}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Address</Label>
                      <p className="font-medium">{searchedCustomer.address}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Status</Label>
                      <p className="font-medium capitalize">{searchedCustomer.status}</p>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => setViewingCustomer(searchedCustomer)}>
                      <Eye className="h-4 w-4 mr-2" />
                      View Details
                    </Button>
                    <Button variant="outline" size="sm" onClick={() => setEditingCustomer(searchedCustomer)}>
                      <Edit className="h-4 w-4 mr-2" />
                      Edit
                    </Button>
                    <Button variant="destructive" size="sm" onClick={() => setDeletingCustomer(searchedCustomer)}>
                      <Trash2 className="h-4 w-4 mr-2" />
                      Delete
                    </Button>
                  </div>
                </CardContent>
              </Card>
            )}
          </CardContent>
        </Card>
      </div>

      <CustomerViewModal 
        customer={viewingCustomer}
        open={!!viewingCustomer}
        onClose={() => setViewingCustomer(null)}
        onEdit={(customer) => setEditingCustomer(customer)}
      />

      <CustomerEditModal 
        customer={editingCustomer}
        open={!!editingCustomer}
        isLoading={isLoading}
        onClose={() => setEditingCustomer(null)}
        onSave={handleEdit}
        onChange={(customer) => setEditingCustomer(customer)}
      />

      {/* Delete Confirmation */}
      <AlertDialog open={!!deletingCustomer} onOpenChange={() => setDeletingCustomer(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete the customer "{deletingCustomer?.name}". This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
              {isLoading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                  Deleting...
                </>
              ) : (
                "Delete"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </DashboardLayout>
  );
};

export default Customers;
