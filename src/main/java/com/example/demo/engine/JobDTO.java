package com.example.demo.engine;

public class JobDTO {
	private String jobId;	// 직업 코드 (예: JOB_DOCTOR)
	private String jobName;	// 직업명
	private String requiredEdu;	// 필요 학력 NONE, ELEM, MIDDLE, HIGH, UNI, GRAD
	private String industry;	// 산업 AGRI, INDUSTRY, SERVICE
	private double incomeRate;	// 소득 배율

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getRequiredEdu() {
		return requiredEdu;
	}

	public void setRequiredEdu(String requiredEdu) {
		this.requiredEdu = requiredEdu;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public double getIncomeRate() {
		return incomeRate;
	}

	public void setIncomeRate(double incomeRate) {
		this.incomeRate = incomeRate;
	}

}
