/*******************************************************************************
 * Copyright (c) 2017 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package eu.vre4eic.evre.nodeservice.usermanager.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import eu.vre4eic.evre.core.Common.UserRole;
import eu.vre4eic.evre.core.impl.EVREUserProfile;


public interface UserProfileRepository extends MongoRepository <EVREUserProfile, String> {
	public EVREUserProfile findByUserId(String userId);
    public List<EVREUserProfile> findByRole(UserRole role);
    public List<EVREUserProfile> findByPassword(String passsword);
    public EVREUserProfile findByEmail(String email);
    
}
