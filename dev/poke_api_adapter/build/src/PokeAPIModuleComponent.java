package com.appresso.ds.dp.modules.adapter.pokeapi;

import com.appresso.ds.dp.spi.AdapterModuleComponent;
import com.appresso.ds.dp.spi.OperationFactory;

public class PokeAPIModuleComponent extends AdapterModuleComponent {
  @Override
  public OperationFactory[] getOperationFactories() throws Exception {
    return new OperationFactory[] { new PokeAPIOperationFactory() };
  }
}
