/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.metamodel.source.internal;

import org.hibernate.MappingException;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.EntityReferencingAttributeBinding;
import org.hibernate.metamodel.source.spi.MetadataImplementor;

/**
 * @author Gail Badner
 */
class EntityReferenceResolver {

	private final MetadataImplementor metadata;

	EntityReferenceResolver(MetadataImplementor metadata) {
		this.metadata = metadata;
	}

	void resolve() {
		for ( EntityBinding entityBinding : metadata.getEntityBindings() ) {
			for ( EntityReferencingAttributeBinding attributeBinding :  entityBinding.getEntityReferencingAttributeBindings() ) {
				resolve( attributeBinding );
			}
		}
	}

	private void resolve(EntityReferencingAttributeBinding attributeBinding) {
		if ( attributeBinding.getReferencedEntityName() == null ) {
			throw new IllegalArgumentException( "attributeBinding has null entityName: " + attributeBinding.getAttribute().getName() );
		}
		EntityBinding entityBinding = metadata.getEntityBinding( attributeBinding.getReferencedEntityName() );
		if ( entityBinding == null ) {
			throw new org.hibernate.MappingException(
					"Attribute [" + attributeBinding.getAttribute().getName() +
					"] refers to unknown entity: [" + attributeBinding.getReferencedEntityName() + "]" );
		}
		AttributeBinding referencedAttributeBinding =
				attributeBinding.isPropertyReference() ?
						entityBinding.getAttributeBinding( attributeBinding.getReferencedAttributeName() ) :
						entityBinding.getEntityIdentifier().getValueBinding();
		if ( referencedAttributeBinding == null ) {
			// TODO: does attribute name include path w/ entity name?
			throw new MappingException(
					"Attribute [" + attributeBinding.getAttribute().getName() +
					"] refers to unknown attribute: [" + attributeBinding.getReferencedEntityName() + "]"
			);
		}
		attributeBinding.resolveReference( referencedAttributeBinding );
		referencedAttributeBinding.addEntityReferencingAttributeBinding( attributeBinding );
	}
}
