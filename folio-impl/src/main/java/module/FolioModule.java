package module;

import com.google.inject.AbstractModule;
import com.knoldus.FolioService;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import impl.FolioServiceImpl;

public class FolioModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {

        bindServices(serviceBinding(FolioService.class, FolioServiceImpl.class));
    }
}
