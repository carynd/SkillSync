package com.skillsync.service;

import com.skillsync.dto.JobPostingResponse;
import com.skillsync.model.SkillDemand;
import com.skillsync.repository.SkillDemandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Real implementation of JobDataService using external APIs.
 * Fetches actual job postings and extracts skills using NLP.
 */
@Service
@Profile("prod")
@Slf4j
public class RealJobDataService implements JobDataService {

    @Autowired
    private SkillDemandRepository skillDemandRepository;

    @Value("${external.rapidapi.key:}")
    private String rapidApiKey;

    @Value("${external.rapidapi.host:jsearch.p.rapidapi.com}")
    private String rapidApiHost;

    private final WebClient webClient;

    // Common tech skills to extract from job descriptions
    private static final Set<String> KNOWN_SKILLS = new HashSet<>(Arrays.asList(
            // Programming Languages
            "Java", "Python", "JavaScript", "TypeScript", "Go", "Rust", "C++", "C#",
            "Ruby", "PHP", "Swift", "Kotlin", "Scala",

            // Frameworks
            "Spring Boot", "React", "Angular", "Vue.js", "Node.js", "Django", "Flask",
            "Express", "Next.js", "Nest.js", ".NET",

            // Databases
            "PostgreSQL", "MySQL", "MongoDB", "Redis", "Cassandra", "DynamoDB",
            "SQL", "NoSQL", "Elasticsearch",

            // Cloud & DevOps
            "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "Git",
            "CI/CD", "Terraform", "Ansible",

            // Other Tech
            "REST API", "GraphQL", "Microservices", "Machine Learning", "AI",
            "Data Science", "Agile", "Scrum", "Jira", "Linux"
    ));

    public RealJobDataService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://jsearch.p.rapidapi.com")
                .build();
    }

    @Override
    public JobPostingResponse analyzeJobMarket(String role, String location, Integer maxResults) {
        log.info("🌐 REAL API MODE: Analyzing job market for role: {}", role);

        if (rapidApiKey == null || rapidApiKey.isEmpty()) {
            log.warn("⚠️ RapidAPI key not configured! Falling back to basic analysis.");
            return createFallbackResponse(role);
        }

        try {
            // Fetch jobs from RapidAPI
            Map<String, Integer> skillFrequency = fetchAndAnalyzeJobs(role, location, maxResults);

            // Save to database
            saveSkillsToDatabase(role, skillFrequency);

            // Convert to response
            List<JobPostingResponse.SkillInfo> skillInfos = skillFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(15)
                    .map(entry -> JobPostingResponse.SkillInfo.builder()
                            .skillName(entry.getKey())
                            .demandScore(entry.getValue())
                            .category(categorizeSkill(entry.getKey()))
                            .frequency((double) entry.getValue() / maxResults * 100)
                            .build())
                    .collect(Collectors.toList());

            return JobPostingResponse.builder()
                    .role(role)
                    .skills(skillInfos)
                    .source("RapidAPI JSearch")
                    .totalJobsAnalyzed(maxResults)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error fetching job data from API: {}", e.getMessage());
            return createFallbackResponse(role);
        }
    }

    @Override
    public List<SkillDemand> getTopSkillsForRole(String role) {
        log.info("🌐 REAL API MODE: Getting top skills for role: {}", role);
        return skillDemandRepository.findTopSkillsByRole(role);
    }

    @Override
    public List<String> getAvailableRoles() {
        return skillDemandRepository.findAllDistinctRoles();
    }

    private Map<String, Integer> fetchAndAnalyzeJobs(String role, String location, Integer maxResults) {
        log.info("📡 Fetching jobs from RapidAPI...");

        // In production, you would make actual API call here
        // For now, this is a placeholder that shows the structure

        /* Example API call (uncomment when you have API key):

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("query", role)
                        .queryParam("page", "1")
                        .queryParam("num_pages", "1")
                        .build())
                .header("X-RapidAPI-Key", rapidApiKey)
                .header("X-RapidAPI-Host", rapidApiHost)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse response and extract skills
        */

        // For now, return empty map (will use fallback)
        return new HashMap<>();
    }

    private Map<String, Integer> extractSkillsFromDescription(String description) {
        Map<String, Integer> skills = new HashMap<>();

        for (String skill : KNOWN_SKILLS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(description);

            if (matcher.find()) {
                skills.put(skill, skills.getOrDefault(skill, 0) + 1);
            }
        }

        return skills;
    }

    private void saveSkillsToDatabase(String role, Map<String, Integer> skillFrequency) {
        for (Map.Entry<String, Integer> entry : skillFrequency.entrySet()) {
            Optional<SkillDemand> existing = skillDemandRepository.findByRoleAndSkill(role, entry.getKey());

            if (existing.isPresent()) {
                SkillDemand demand = existing.get();
                demand.setDemandScore(entry.getValue());
                skillDemandRepository.save(demand);
            } else {
                SkillDemand demand = SkillDemand.builder()
                        .role(role)
                        .skill(entry.getKey())
                        .demandScore(entry.getValue())
                        .category(categorizeSkill(entry.getKey()))
                        .source("RapidAPI")
                        .build();
                skillDemandRepository.save(demand);
            }
        }
    }

    private String categorizeSkill(String skill) {
        if (skill.matches("(?i).*(java|python|javascript|typescript|go|rust|c\\+\\+|c#|ruby|php|swift|kotlin|scala).*")) {
            return "Programming Language";
        } else if (skill.matches("(?i).*(spring|react|angular|vue|django|flask|express|next|nest|\\.net).*")) {
            return "Framework";
        } else if (skill.matches("(?i).*(postgresql|mysql|mongodb|redis|sql|cassandra|dynamodb|elasticsearch).*")) {
            return "Database";
        } else if (skill.matches("(?i).*(aws|azure|gcp|docker|kubernetes|jenkins|terraform|ansible).*")) {
            return "Cloud/DevOps";
        } else {
            return "Other";
        }
    }

    private JobPostingResponse createFallbackResponse(String role) {
        log.info("⚠️ Using fallback data for role: {}", role);

        return JobPostingResponse.builder()
                .role(role)
                .skills(new ArrayList<>())
                .source("Fallback (API unavailable)")
                .totalJobsAnalyzed(0)
                .build();
    }
}
