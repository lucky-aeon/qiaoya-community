package org.xhy.community.application.course.transcript;

public interface TranscriptProvider {

    TranscriptSubmitResult submit(TranscriptSubmitCommand command);

    TranscriptProviderTaskResult query(String providerTaskId);
}
