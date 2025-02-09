-- Create the Users table (no hashing)
CREATE TABLE Users (
    userID VARCHAR(255) PRIMARY KEY,               -- User ID (Primary Key)
    organization VARCHAR(255) NOT NULL,            -- Organization name
    serialNumber VARCHAR(255) NOT NULL,            -- Serial Number
    uniqueID VARCHAR(255) NOT NULL,                -- Unique ID
    emailAddress VARCHAR(255) NOT NULL             -- Email Address
);

-- Insert sample data into the Users table
INSERT INTO Users (userID, organization, serialNumber, uniqueID, emailAddress)
VALUES
('user1', 'Company A', 'Device123', 'uniqueID123', 'support@companya.com');

-- Create the VerifiedUsers table (no hashing)
CREATE TABLE VerifiedUsers (
    id SERIAL PRIMARY KEY,                         -- Auto-generated primary key
    organization VARCHAR(255) NOT NULL,            -- Organization name
    organizationSupportEmail VARCHAR(255) NOT NULL, -- Support email of the organization
    registeredDevice VARCHAR(255) NOT NULL,        -- Device that the user is registered with (serial number)
    uniqueID VARCHAR(255) NOT NULL,                -- Unique ID to verify user
    emailAddress VARCHAR(255) NOT NULL             -- Email address for verification
);

-- Insert sample data into the VerifiedUsers table
INSERT INTO VerifiedUsers (organization, organizationSupportEmail, registeredDevice, uniqueID, emailAddress)
VALUES
('Company A', 'support@companya.com', 'Device123', 'uniqueID123', 'support@companya.com'),
('Company B', 'support@companyb.com', 'Device456', 'uniqueID456', 'support@companyb.com');

