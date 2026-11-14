package com.alumnisearch.util;

import com.alumnisearch.dto.response.AlumniProfileResponse;
import com.alumnisearch.entity.AlumniProfile;
import com.alumnisearch.scraper.ScrapedAlumniRecord;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AlumniDataMapper {

    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(19\\d\\d|20\\d\\d)\\b");

    public AlumniProfileResponse toResponse(AlumniProfile entity) {
        if (entity == null) {
            return null;
        }

        return AlumniProfileResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .currentRole(entity.getCurrentRole())
                .university(entity.getUniversity())
                .location(entity.getLocation())
                .linkedinHeadline(entity.getLinkedinHeadline())
                .passoutYear(entity.getPassoutYear())
                .linkedinUrl(entity.getLinkedinUrl())
                .company(entity.getCompany())
                .build();
    }

    public AlumniProfile toEntity(ScrapedAlumniRecord record, String defaultUniversity, String defaultDesignation, String searchQuery) {
        if (record == null) {
            return null;
        }

        String university = (record.getUniversity() != null && !record.getUniversity().isBlank())
                ? record.getUniversity().trim()
                : defaultUniversity;

        String currentRole = (record.getCurrentRole() != null && !record.getCurrentRole().isBlank())
                ? record.getCurrentRole().trim()
                : defaultDesignation;

        return AlumniProfile.builder()
                .name(record.getName() != null ? record.getName().trim() : "LinkedIn Member")
                .currentRole(currentRole)
                .university(university)
                .location(record.getLocation())
                .linkedinHeadline(record.getLinkedinHeadline())
                .passoutYear(record.getPassoutYear())
                .linkedinUrl(record.getLinkedinUrl())
                .company(record.getCompany())
                .searchQuery(searchQuery)
                .build();
    }

    /**
     * Adapts dynamic field names from PhantomBuster LinkedIn Search or Profile Scraper CSV/JSON
     * into a standardized ScrapedAlumniRecord.
     */
    public ScrapedAlumniRecord fromRawMap(Map<String, Object> map, String defaultUniversity, String defaultDesignation, Integer targetYear) {
        if (map == null) {
            return null;
        }

        String name = extractString(map, "name", "fullName", "profileName", "title");
        String currentRole = extractString(map, "currentRole", "occupation", "headline", "jobTitle", "designation");
        String headline = extractString(map, "linkedinHeadline", "headline", "summary", "description");
        String location = extractString(map, "location", "city", "geographicArea");
        String university = extractString(map, "university", "school", "education", "college");
        String company = extractString(map, "company", "companyName", "currentCompany");
        String profileUrl = extractString(map, "linkedinUrl", "profileUrl", "url", "link");

        if (name == null || name.isBlank()) {
            name = "LinkedIn Member";
        }
        if (currentRole == null || currentRole.isBlank()) {
            currentRole = defaultDesignation;
        }
        if (university == null || university.isBlank()) {
            university = defaultUniversity;
        }
        if (headline == null || headline.isBlank()) {
            headline = currentRole + (company != null ? " at " + company : "");
        }

        Integer passoutYear = extractPassoutYear(map, targetYear, headline);

        return ScrapedAlumniRecord.builder()
                .name(name)
                .currentRole(currentRole)
                .university(university)
                .location(location != null ? location : "Global")
                .linkedinHeadline(headline)
                .passoutYear(passoutYear)
                .linkedinUrl(profileUrl)
                .company(company)
                .build();
    }

    private String extractString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) {
                String str = String.valueOf(val).trim();
                if (!str.isEmpty() && !str.equalsIgnoreCase("null")) {
                    return str;
                }
            }
        }
        return null;
    }

    private Integer extractPassoutYear(Map<String, Object> map, Integer targetYear, String headline) {
        for (String key : new String[]{"passoutYear", "graduationYear", "year", "educationYear"}) {
            Object val = map.get(key);
            if (val instanceof Number num) {
                return num.intValue();
            } else if (val != null) {
                try {
                    return Integer.parseInt(String.valueOf(val).trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        // Try extracting year from text headline or education if present
        if (headline != null) {
            Matcher matcher = YEAR_PATTERN.matcher(headline);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }

        return targetYear;
    }
}
