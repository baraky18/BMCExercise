package com.exercise.Bmc;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;

@SpringBootApplication
public class BmcApplication {

	private static String bucketName = "bmctestbucket";
	private static String accessKey = "J00IKAR80DUAYSFMD0C5";
	private static String secretKey = "Pvx/=TnTcz0IB6q+eOnuh=iw/dKcloS1gXV9mI6E";
	private static String sandboxEndpoint = "https://9546b1f5-bcb7-11eb-a480-6644fae49104.sandbox.zenko.io";
	private static String filesFolder = "C:/Users/baraky/git/BMCTest/Bmc/src/main/resources/files";

	public static void main(String[] args) throws FileNotFoundException {
		SpringApplication.run(BmcApplication.class, args);
		
		AmazonS3 s3client = loginToOrbitSandbox();
		TransferManager transferMgr = TransferManagerBuilder.standard().withS3Client(s3client).build();
		File sourceFolder = new File(filesFolder);
		File[] files = sourceFolder.listFiles();
		uploadFilesToSandbox(transferMgr, files);
		transferMgr.shutdownNow();
	}

	private static void uploadFilesToSandbox(TransferManager transferMgr, File[] files) {
		if (files != null) {
			System.out.println(files.length + " files are about to be transferred to bucket " + bucketName);
			for (File file : files) {
				System.out.println("Transferring " + file.getName());
				Upload upload = transferMgr.upload(bucketName, file.getName(), file);
				TransferProgress transferProgress = upload.getProgress();
				int percentage = 0;
				while(!upload.isDone()){
					if(transferProgress.getPercentTransferred() >= percentage){
						System.out.println("Transferred: " + percentage*(transferProgress.getTotalBytesToTransfer()/100) + " bytes Percent: " + percentage);
						percentage+=10;
					}
				}
			}
		}
	}

	private static AmazonS3 loginToOrbitSandbox() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		EndpointConfiguration endpointConfiguration = 
				new EndpointConfiguration(sandboxEndpoint, Regions.US_EAST_2.toString());
		AmazonS3 s3client = AmazonS3Client.builder()
				.withCredentials(new AWSStaticCredentialsProvider(credentials))
				.enablePathStyleAccess()
				.disableChunkedEncoding()
				.withEndpointConfiguration(endpointConfiguration)
				.build();
		return s3client;
	}

}
